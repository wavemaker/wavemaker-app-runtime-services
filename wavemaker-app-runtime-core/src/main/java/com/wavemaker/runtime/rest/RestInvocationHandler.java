package com.wavemaker.runtime.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.commons.WMObjectMapper;
import com.wavemaker.runtime.rest.model.HttpRequestData;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;
import com.wavemaker.runtime.rest.service.RestRuntimeService;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public class RestInvocationHandler implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(RestInvocationHandler.class);

    private static Pattern pathParameterPattern = Pattern.compile("\\/\\{(\\w*)\\}");
    private static Pattern queryParameterPattern = Pattern.compile("=\\{(\\w*)\\}");

    private RestRuntimeService restRuntimeService;

    private String serviceId;

    public RestInvocationHandler(String serviceId, RestRuntimeService restRuntimeService) {
        this.serviceId = serviceId;
        this.restRuntimeService = restRuntimeService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        HttpRequestData httpRequestData = new HttpRequestData();
        Map<String, String> pathVariablesMap = new HashMap<>();
        MultiValueMap<String, String> queryVariablesMap = new LinkedMultiValueMap<>();

        List<String> queryVariablesList = getQueryVariables(method.getAnnotation(RequestLine.class).value());

        int position = 0;
        for (Annotation[] parameterAnnotation : method.getParameterAnnotations()) {
            if (args[position] != null) {
                if (parameterAnnotation.length != 0 && parameterAnnotation[0] instanceof Param) {
                    if (queryVariablesList.contains(((Param) parameterAnnotation[0]).value())) {
                        queryVariablesMap.add(((Param) parameterAnnotation[0]).value(), args[position].toString());
                    } else {
                        pathVariablesMap.put(((Param) parameterAnnotation[0]).value(), args[position].toString());
                    }
                } else if (parameterAnnotation.length != 0 && parameterAnnotation[0] instanceof QueryMap) {
                    queryVariablesMap.addAll((MultiValueMap<String, String>) args[position]);
                } else {
                    //set as request body
                    httpRequestData.setRequestBody(new ByteArrayInputStream(WMObjectMapper.getInstance().writeValueAsBytes(args[position])));
                    httpRequestData.getHttpHeaders().add("content-type", "application/json");
                    //TODO:file
                }
            }
            position++;
        }

        httpRequestData.setQueryParametersMap(queryVariablesMap);
        httpRequestData.setPathVariablesMap(pathVariablesMap);

//        logger.info("constructed request data {}", httpRequestData.getPathVariablesMap());
//        logger.info("constructed request query param {}", httpRequestData.getQueryParametersMap());

        String[] split = method.getAnnotation(RequestLine.class).value().split(" ");
        HttpResponseDetails responseDetails = restRuntimeService.executeRestCall(serviceId,
                split[1].contains("?") ? split[1].subSequence(0, split[1].indexOf("?")).toString() : split[1],
                split[0],
                httpRequestData, RestExecutor.getRequestContextThreadLocal());

        try {
            if (method.getReturnType() != void.class) {
                if (responseDetails.getStatusCode() >= 200 && responseDetails.getStatusCode() < 300) {
                    return WMObjectMapper.getInstance().readValue(responseDetails.getBody(), method.getReturnType());
                } else {
                    logger.error(IOUtils.toString(responseDetails.getBody(), Charset.defaultCharset()));
                    throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.$RestServiceInvocationError"), responseDetails.getStatusCode());
                }

            }
        } catch (IOException e) {
            throw new WMRuntimeException(e);
        }
        return null;
    }

    private Queue<String> getPathVariables(String value) {
        Queue<String> arrayList = new LinkedList<>();
        Matcher matcher = pathParameterPattern.matcher(value);
        while (matcher.find()) {
            arrayList.add(matcher.group(1));
        }
        return arrayList;
    }

    private List<String> getQueryVariables(String value) {
        List<String> queryVariables = new ArrayList();
        Matcher matcher = queryParameterPattern.matcher(value);
        while (matcher.find()) {
            queryVariables.add(matcher.group(1));
        }
        return queryVariables;

    }
}
