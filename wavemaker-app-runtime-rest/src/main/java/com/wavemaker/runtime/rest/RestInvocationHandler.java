package com.wavemaker.runtime.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public class RestInvocationHandler implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(RestInvocationHandler.class);

    private static Pattern pathParameterPattern = Pattern.compile("\\/\\{(\\w*)\\}");
    private static Pattern queryParameterPattern = Pattern.compile("=\\{(\\w*)\\}");
    private static Pattern headerParameterPattern = Pattern.compile("\\{(\\w*)\\}");
    private static Pattern splitHeaderPattern = Pattern.compile("(.*):\\s\\{*((\\w|\\/)*)\\}*");

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
        Map<String, String> headerVariableMap = new HashMap<>();

        List<String> queryVariablesList = getQueryVariables(method.getAnnotation(RequestLine.class).value());
        List<String> headerPlaceholders = extractHeaderPlaceholders(method.getAnnotation(Headers.class).value());

        int position = 0;
        for (Annotation[] parameterAnnotation : method.getParameterAnnotations()) {
            if (args[position] != null) {
                if (parameterAnnotation.length != 0 && parameterAnnotation[0] instanceof Param) {
                    if (queryVariablesList.contains(((Param) parameterAnnotation[0]).value())) {
                        queryVariablesMap.add(((Param) parameterAnnotation[0]).value(), args[position].toString());
                    } else if (headerPlaceholders.contains(((Param) parameterAnnotation[0]).value())) {
                        headerVariableMap.put(((Param) parameterAnnotation[0]).value(), args[position].toString());
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

        logger.debug("constructed request data {}", httpRequestData.getPathVariablesMap());
        logger.debug("constructed request query param {}", httpRequestData.getQueryParametersMap());

        //Resolving the headers and setting them to httpRequestData
        //eg: if Header is like X-RapidAPI-Key: {x_RapidAPI_Key} in this case we will look for x_RapidAPI_Key in variableMap and set its value
        Arrays.stream(method.getAnnotation(Headers.class).value()).forEach(header -> {
            Matcher matcher = splitHeaderPattern.matcher(header);
            while (matcher.find()) {
                httpRequestData.getHttpHeaders().add(matcher.group(1), header.contains("{") ? headerVariableMap.get(matcher.group(2)) :
                        matcher.group(2));
            }
        });

        String[] split = method.getAnnotation(RequestLine.class).value().split(" ");
        HttpResponseDetails responseDetails = restRuntimeService.executeRestCall(serviceId,
                split[1].contains("?") ? split[1].subSequence(0, split[1].indexOf("?")).toString() : split[1],
                split[0],
                httpRequestData, RestExecutor.getRequestContextThreadLocal());

        try {
            if (method.getReturnType() != void.class) {
                if (responseDetails.getStatusCode() >= 200 && responseDetails.getStatusCode() < 300) {
                    if (method.getGenericReturnType() instanceof ParameterizedType) {
                        return WMObjectMapper.getInstance().readValue(responseDetails.getBody(),
                                WMObjectMapper.getInstance().getTypeFactory()
                                        .constructCollectionType((Class<? extends Collection>) Class.forName(method.getReturnType().getName()),
                                                Class.forName(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0].getTypeName())));
                    }
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

    private List<String> extractHeaderPlaceholders(String[] value) {
        List<String> headerVariables = new ArrayList();
        Arrays.stream(value).forEach(header -> {
            Matcher matcher = headerParameterPattern.matcher(header);
            while (matcher.find()) {
                headerVariables.add(matcher.group(1));
            }
        });
        return headerVariables;
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
