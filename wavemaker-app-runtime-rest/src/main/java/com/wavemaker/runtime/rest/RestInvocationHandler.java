/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wavemaker.runtime.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.rest.WmFileSystemResource;
import com.wavemaker.runtime.commons.WMObjectMapper;
import com.wavemaker.runtime.rest.model.HttpRequestData;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;
import com.wavemaker.runtime.rest.model.Message;
import com.wavemaker.runtime.rest.service.RestRuntimeService;
import com.wavemaker.runtime.rest.util.HttpRequestUtils;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public class RestInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestInvocationHandler.class);

    private static final Pattern pathParameterPattern = Pattern.compile("\\/\\{(\\w*)\\}");
    private static final Pattern queryParameterPattern = Pattern.compile("=\\{(\\w*)\\}");
    private static final Pattern headerParameterPattern = Pattern.compile("\\{(\\w*)\\}");
    private static final Pattern splitHeaderPattern = Pattern.compile("([^:]+):\\s*(.+)");

    private RestRuntimeService restRuntimeService;

    private EncodingMode encodingMode;

    private String serviceId;

    public RestInvocationHandler(String serviceId, RestRuntimeService restRuntimeService, EncodingMode encodingMode) {
        this.serviceId = serviceId;
        this.restRuntimeService = restRuntimeService;
        this.encodingMode = encodingMode;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        HttpRequestData httpRequestData = new HttpRequestData();
        Map<String, String> pathVariablesMap = new HashMap<>();
        MultiValueMap<String, String> queryVariablesMap = new LinkedMultiValueMap<>();
        Map<String, String> headerVariableMap = new HashMap<>();
        MultiValueMap<String, Object> formVariableMap = new LinkedMultiValueMap<>();
        List<String> queryVariablesList = getQueryVariables(method.getAnnotation(RequestLine.class).value());
        List<String> pathVariablesList = getPathVariables(method.getAnnotation(RequestLine.class).value());
        List<String> headerPlaceholders = extractHeaderPlaceholders(method.getAnnotation(Headers.class).value());
        boolean urlEncodedHeader = isUrlEncodedHeaderPresent(method.getAnnotation(Headers.class).value());
        boolean multipartFormDataHeader = isMultipartFormDataHeaderPresent(method.getAnnotation(Headers.class).value());
        int position = 0;
        for (Annotation[] parameterAnnotation : method.getParameterAnnotations()) {
            if (args[position] != null) {
                if (parameterAnnotation.length != 0 && parameterAnnotation[0] instanceof Param) {
                    if (queryVariablesList.contains(((Param) parameterAnnotation[0]).value())) {
                        queryVariablesMap.add(((Param) parameterAnnotation[0]).value(), args[position].toString());
                    } else if (headerPlaceholders.contains(((Param) parameterAnnotation[0]).value())) {
                        headerVariableMap.put(((Param) parameterAnnotation[0]).value(), args[position].toString());
                    } else if (pathVariablesList.contains(((Param) parameterAnnotation[0]).value())) {
                        pathVariablesMap.put(((Param) parameterAnnotation[0]).value(), args[position].toString());
                    } else if (urlEncodedHeader || multipartFormDataHeader) {
                        formVariableMap.add(((Param) parameterAnnotation[0]).value(), args[position]);
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

        if (!formVariableMap.isEmpty()) {
            Message formMessage = null;
            if (multipartFormDataHeader) {
                MultiValueMap<String, Object> convertedMap = convertToMultipartData(formVariableMap);
                formMessage = HttpRequestUtils.createMessage(convertedMap, MediaType.MULTIPART_FORM_DATA_VALUE);
            } else if (urlEncodedHeader) {
                formMessage = HttpRequestUtils.createMessage(formVariableMap, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            }
            if (formMessage != null) {
                httpRequestData.setRequestBody(formMessage.getInputStream());
                httpRequestData.getHttpHeaders().addAll(formMessage.getHttpHeaders());
            }
        }
        logger.debug("constructed request data {}", httpRequestData.getPathVariablesMap());
        logger.debug("constructed request query param {}", httpRequestData.getQueryParametersMap());

        //Resolving the headers and setting them to httpRequestData
        //eg: if Header is like X-RapidAPI-Key: {x_RapidAPI_Key} in this case we will look for x_RapidAPI_Key in variableMap and set its value
        Arrays.stream(method.getAnnotation(Headers.class).value())
            .map(splitHeaderPattern::matcher)
            .filter(Matcher::find)
            .forEach(matcher -> {
                String headerName = matcher.group(1);
                String headerValue = matcher.group(2);
                if (headerValue.startsWith("{") && headerValue.endsWith("}")) {
                    headerValue = headerVariableMap.getOrDefault(headerValue.substring(1, headerValue.length() - 1), headerValue);
                }
                httpRequestData.getHttpHeaders().addIfAbsent(headerName, headerValue);
            });
        String[] split = method.getAnnotation(RequestLine.class).value().split(" ");
        HttpResponseDetails responseDetails = restRuntimeService.executeRestCall(serviceId,
            split[1].contains("?") ? split[1].subSequence(0, split[1].indexOf("?")).toString() : split[1],
            split[0],
            httpRequestData, RestExecutor.getRequestContextThreadLocal(), encodingMode);

        try {
            if (method.getReturnType() != void.class) {
                if (responseDetails.getStatusCode() >= 200 && responseDetails.getStatusCode() < 300) {
                    if (method.getGenericReturnType() instanceof ParameterizedType) {
                        return WMObjectMapper.getInstance().readValue(responseDetails.getBody(),
                            WMObjectMapper.getInstance().getTypeFactory()
                                .constructCollectionType((Class<? extends Collection>) Class.forName(method.getReturnType().getName()),
                                    Class.forName(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0].getTypeName(),
                                        true, Thread.currentThread().getContextClassLoader())));
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

    private MultiValueMap<String, Object> convertToMultipartData(MultiValueMap<String, Object> formVariableMap) {
        MultiValueMap<String, Object> convertedMap = new LinkedMultiValueMap<>();
        formVariableMap.forEach((key, value) -> {
            if (value != null) {
                if (value.get(0) instanceof List<?> listValue) {
                    if (!listValue.isEmpty() && listValue.get(0) instanceof File) {
                        for (Object item : listValue) {
                            if (item instanceof File) {
                                addFileToConvertMap((File) item, convertedMap, key);
                            }
                        }
                    }
                } else {
                    convertedMap.add(key, value.get(0));
                }
            }
        });
        return convertedMap;
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

    private List<String> getPathVariables(String value) {
        List<String> arrayList = new LinkedList<>();
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

    private void addFileToConvertMap(File file, MultiValueMap<String, Object> convertedMap, String key) {
        WmFileSystemResource fileSystemResource = new WmFileSystemResource(file, MediaType.MULTIPART_FORM_DATA_VALUE);
        convertedMap.add(key, fileSystemResource);
    }

    private boolean isUrlEncodedHeaderPresent(String[] headerList) {
        return Arrays.stream(headerList)
            .anyMatch(header -> header.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
    }

    private boolean isMultipartFormDataHeaderPresent(String[] headerList) {
        return Arrays.stream(headerList)
            .anyMatch(header -> header.contains(MediaType.MULTIPART_FORM_DATA_VALUE));
    }
}
