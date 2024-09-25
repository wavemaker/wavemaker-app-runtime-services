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
package com.wavemaker.runtime.rest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import org.springframework.web.util.UriBuilder;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.UnAuthorizedResourceAccessException;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.comparator.UrlComparator;
import com.wavemaker.commons.comparator.UrlStringComparator;
import com.wavemaker.commons.swaggerdoc.util.SwaggerDocUtil;
import com.wavemaker.commons.web.filter.RequestTrackingFilter;
import com.wavemaker.commons.web.filter.ServerTimingMetric;
import com.wavemaker.runtime.commons.util.PropertyPlaceHolderReplacementHelper;
import com.wavemaker.runtime.rest.RequestContext;
import com.wavemaker.runtime.rest.RequestDataBuilder;
import com.wavemaker.runtime.rest.RestConstants;
import com.wavemaker.runtime.rest.model.HttpRequestData;
import com.wavemaker.runtime.rest.model.HttpRequestDetails;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;
import com.wavemaker.runtime.rest.processor.RestRuntimeConfig;
import com.wavemaker.runtime.rest.processor.data.HttpRequestDataProcessor;
import com.wavemaker.runtime.rest.processor.data.HttpRequestDataProcessorContext;
import com.wavemaker.runtime.rest.processor.request.HttpRequestProcessor;
import com.wavemaker.runtime.rest.processor.request.HttpRequestProcessorContext;
import com.wavemaker.runtime.rest.processor.response.HttpResponseProcessor;
import com.wavemaker.runtime.rest.processor.response.HttpResponseProcessorContext;
import com.wavemaker.runtime.rest.util.HttpRequestUtils;
import com.wavemaker.runtime.rest.util.RestRequestUtils;
import com.wavemaker.tools.apidocs.tools.core.model.Operation;
import com.wavemaker.tools.apidocs.tools.core.model.ParameterType;
import com.wavemaker.tools.apidocs.tools.core.model.Path;
import com.wavemaker.tools.apidocs.tools.core.model.Swagger;
import com.wavemaker.tools.apidocs.tools.core.model.auth.ApiKeyAuthDefinition;
import com.wavemaker.tools.apidocs.tools.core.model.auth.BasicAuthDefinition;
import com.wavemaker.tools.apidocs.tools.core.model.auth.OAuth2Definition;
import com.wavemaker.tools.apidocs.tools.core.model.auth.SecuritySchemeDefinition;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.Parameter;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.RefParameter;
import com.wavemaker.tools.apidocs.tools.core.utils.PathUtils;

/**
 * @author Uday Shankar
 */
public class RestRuntimeService {

    private static final String AUTHORIZATION = "authorization";
    private static final Logger logger = LoggerFactory.getLogger(RestRuntimeService.class);
    private static final String OPERATION_DOES_NOT_EXIST = "com.wavemaker.runtime.operation.doesnt.exist";
    private RestRuntimeServiceCacheHelper restRuntimeServiceCacheHelper;

    @Autowired
    private RequestTrackingFilter requestTrackingFilter;

    @Autowired
    private PropertyPlaceHolderReplacementHelper propertyPlaceHolderReplacementHelper;

    @Autowired
    private Environment environment;

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private RestConnector restConnector;

    @PostConstruct
    public void init() {
        restRuntimeServiceCacheHelper = new RestRuntimeServiceCacheHelper();
        restRuntimeServiceCacheHelper.setPropertyPlaceHolderReplacementHelper(propertyPlaceHolderReplacementHelper);
        restRuntimeServiceCacheHelper.setEnvironment(environment);
    }

    /**
     * @deprecated : This method is deprecated
     * <p>
     * The recommended way to make the rest api calls from java service is by using <a href="https://docs.wavemaker.com/learn/app-development/services/java-services/api-composer-toolkit">API Orchestration</a>
     * </p>
     */
    @Deprecated
    public HttpResponseDetails executeRestCall(String serviceId, String operationId, HttpRequestData httpRequestData) {
        return executeRestCall(serviceId, operationId, httpRequestData, null);
    }

    /**
     * @deprecated : This method is deprecated
     * <p>
     * The recommended way to make the rest api calls from java service is by using <a href="https://docs.wavemaker.com/learn/app-development/services/java-services/api-composer-toolkit">API Orchestration</a>
     * </p>
     */
    @Deprecated
    public HttpResponseDetails executeRestCall(String serviceId, String operationId, HttpRequestData httpRequestData,
                                               HttpServletRequest httpServletRequest) {
        EncodingMode encodingMode = environment.getProperty("app.rest.apiorchestration.encoding.mode", EncodingMode.class,
            EncodingMode.TEMPLATE_AND_VALUES);
        Swagger swagger = restRuntimeServiceCacheHelper.getSwaggerDoc(serviceId);
        Triple<String, Path, Operation> pathAndOperation = findPathAndOperation(swagger, operationId);
        HttpRequestDetails httpRequestDetails = constructHttpRequest(serviceId, pathAndOperation.getLeft(),
            SwaggerDocUtil.getOperationType(pathAndOperation.getMiddle(), pathAndOperation.getRight().getOperationId()).toUpperCase(), httpRequestData,
            null, encodingMode);
        return executeRestCallWithProcessors(serviceId, httpRequestDetails, httpRequestData, httpServletRequest, "");
    }

    public void executeRestCall(String serviceId, String path, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                EncodingMode encodingMode) {
        HttpRequestData httpRequestData = constructRequestData(httpServletRequest);
        HttpRequestDataProcessorContext httpRequestDataProcessorContext = new HttpRequestDataProcessorContext(httpServletRequest, httpRequestData);
        List<HttpRequestDataProcessor> httpRequestDataProcessors = restRuntimeServiceCacheHelper.getHttpRequestDataProcessors(serviceId);
        String context = httpServletRequest.getRequestURI();
        for (HttpRequestDataProcessor httpRequestDataProcessor : httpRequestDataProcessors) {
            logger.debug("Executing the httpRequestDataProcessor {} on the context {}", httpRequestDataProcessor, context);
            httpRequestDataProcessor.process(httpRequestDataProcessorContext);
        }
        HttpRequestDetails httpRequestDetails = constructHttpRequest(serviceId, path, httpServletRequest.getMethod(), httpRequestData, null, encodingMode);
        HttpResponseDetails httpResponseDetails = executeRestCallWithProcessors(serviceId, httpRequestDetails, httpRequestData, httpServletRequest, context);
        try {
            HttpRequestUtils.writeResponse(httpResponseDetails, httpServletResponse);
        } catch (IOException e) {
            throw new WMRuntimeException(e);
        }
    }

    public HttpResponseDetails executeRestCall(String serviceId, String path, String method, HttpRequestData httpRequestData,
                                               RequestContext requestContext, EncodingMode encodingMode) {
        HttpRequestDetails httpRequestDetails = constructHttpRequest(serviceId, path, method, httpRequestData, requestContext, encodingMode);
        return executeRestCallWithProcessors(serviceId, httpRequestDetails, httpRequestData, null, "");
    }

    private HttpResponseDetails executeRestCallWithProcessors(String serviceId, HttpRequestDetails httpRequestDetails, final HttpRequestData httpRequestData,
                                                              final HttpServletRequest httpServletRequest, final String context) {
        HttpRequestProcessorContext httpRequestProcessorContext = new HttpRequestProcessorContext(httpServletRequest, httpRequestDetails, httpRequestData);
        final RestRuntimeConfig restRuntimeConfig = restRuntimeServiceCacheHelper.getAppRuntimeConfig(serviceId);
        List<HttpRequestProcessor> httpRequestProcessors = restRuntimeConfig.getHttpRequestProcessorList();
        for (HttpRequestProcessor httpRequestProcessor : httpRequestProcessors) {
            logger.debug("Executing the httpRequestProcessor {} on the context {}", httpRequestProcessor, context);
            httpRequestProcessor.process(httpRequestProcessorContext);
        }

        logger.debug("Rest service request details {}", httpRequestDetails);

        HttpResponseDetails httpResponseDetails = new HttpResponseDetails();
        long start = System.currentTimeMillis();
        restConnector.invokeRestCall(httpRequestDetails, response -> {
            long processingTime = System.currentTimeMillis() - start;
            logger.info("Time taken by the rest server endpoint is {}", processingTime);
            requestTrackingFilter.addServerTimingMetrics(new ServerTimingMetric("rest-server", processingTime, null));

            try {
                httpResponseDetails.setStatusCode(response.getStatusCode().value());
                httpResponseDetails.setBody(response.getBody());
            } catch (IOException e) {
                throw new WMRuntimeException(e);
            }
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.putAll(response.getHeaders());
            httpResponseDetails.setHeaders(httpHeaders);

            HttpResponseProcessorContext httpResponseProcessorContext = new HttpResponseProcessorContext(httpServletRequest, httpResponseDetails, httpRequestDetails, httpRequestData);
            List<HttpResponseProcessor> httpResponseProcessors = restRuntimeConfig.getHttpResponseProcessorList();
            for (HttpResponseProcessor httpResponseProcessor : httpResponseProcessors) {
                logger.debug("Executing the httpResponseProcessor {} on the context {}", httpResponseProcessor, context);
                httpResponseProcessor.process(httpResponseProcessorContext);
            }

            logger.debug("Rest service response details for the context {} is {}", context, httpResponseDetails);
        });
        return httpResponseDetails;
    }

    private HttpRequestData constructRequestData(HttpServletRequest httpServletRequest) {
        HttpRequestData httpRequestData;
        try {
            httpRequestData = new RequestDataBuilder().getRequestData(httpServletRequest);
        } catch (Exception e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.HttpRequestData.construction.failed"), e);
        }
        return httpRequestData;
    }

    private HttpRequestDetails constructHttpRequest(String serviceId, String path, String method, HttpRequestData httpRequestData,
                                                    RequestContext requestContext, EncodingMode encodingMode) {
        Swagger swagger = restRuntimeServiceCacheHelper.getSwaggerDoc(serviceId);
        final Pair<String, Operation> operationPair = findOperation(swagger, path, method);

        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, Object> queryParameters = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        Operation operation = operationPair.getRight();
        filterAndApplyServerVariablesOnRequestData(httpRequestData, swagger, operation,
            httpHeaders, queryParameters, pathParameters);

        HttpRequestDetails httpRequestDetails = new HttpRequestDetails();

        updateAuthorizationInfo(serviceId, swagger.getSecurityDefinitions(), operation, queryParameters, httpHeaders, httpRequestData);
        updateUrlWithRequestContext(requestContext, queryParameters, httpHeaders);
        String endPointAddress = getEndPointAddress(serviceId, operationPair.getLeft(), queryParameters, pathParameters, encodingMode);
        httpRequestDetails.setEndpointAddress(endPointAddress);
        httpRequestDetails.setMethod(method);

        httpRequestDetails.setHeaders(httpHeaders);
        httpRequestDetails.setBody(httpRequestData.getRequestBody());

        return httpRequestDetails;
    }

    private String getEndPointAddress(String serviceId, String pathValue, Map<String, Object> queryParameters, Map<String, String> pathParameters,
                                      EncodingMode encodingMode) {
        String scheme = getPropertyValue(serviceId, RestConstants.SCHEME_KEY);
        String host = getPropertyValue(serviceId, RestConstants.HOST_KEY);
        String basePath = getPropertyValue(serviceId, RestConstants.BASE_PATH_KEY);

        String url = new StringBuilder(scheme).append("://").append(host)
            .append(getNormalizedString(basePath)).append(getNormalizedString(pathValue)).toString();

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        encodingMode = (encodingMode != null) ? encodingMode : EncodingMode.TEMPLATE_AND_VALUES;
        uriBuilderFactory.setEncodingMode(encodingMode);

        UriBuilder uriBuilder = uriBuilderFactory.uriString(url);
        updateUrlWithQueryParams(uriBuilder, queryParameters);
        return uriBuilder.build(pathParameters).toString();
    }

    private void updateUrlWithQueryParams(UriBuilder uriBuilder, Map<String, Object> queryParameters) {
        queryParameters.forEach((key, valueList) -> {
            if (valueList instanceof List<?>) {
                ((List<?>) valueList).forEach(value -> uriBuilder.queryParam(key, value));
            } else {
                uriBuilder.queryParam(key, valueList);
            }
        });
    }

    private void updateUrlWithRequestContext(RequestContext requestContext, Map<String, Object> queryParameters, HttpHeaders headers) {
        if (requestContext != null) {
            if (requestContext.getHeaders() != null) {
                headers.putAll(requestContext.getHeaders());
            }
            if (requestContext.getQueryParams() != null) {
                queryParameters.putAll(requestContext.getQueryParams());
            }
        }
    }

    private Triple<String, Path, Operation> findPathAndOperation(Swagger swagger, String operationId) {
        for (final Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            for (final Operation operation : pathEntry.getValue().getOperations()) {
                if (operation.getMethodName().equalsIgnoreCase(operationId)) {
                    return ImmutableTriple.of(pathEntry.getKey(), pathEntry.getValue(), operation);
                }
            }
        }
        throw new WMRuntimeException(MessageResource.create(OPERATION_DOES_NOT_EXIST),
            operationId);
    }

    private Pair<String, Operation> findOperation(Swagger swagger, String path, String method) {
        Map<String, Path> swaggerPaths = swagger.getPaths();

        List<String> pathEntries = new ArrayList<>(swaggerPaths.keySet());
        pathEntries.sort(new UrlComparator<>() {
            @Override
            public String getUrlPattern(String s) {
                return s;
            }
        });
        pathEntries.sort(new UrlStringComparator<>() {
            @Override
            public String getUrlPattern(String s) {
                return s;
            }
        });

        for (String pathString : pathEntries) {
            if (pathMatcher.match(pathString, path)) {
                Operation operation = PathUtils.getOperation(swaggerPaths.get(pathString), method);
                if (operation != null) {
                    return ImmutablePair.of(pathString, operation);
                }
            }
        }
        throw new WMRuntimeException(MessageResource.create(OPERATION_DOES_NOT_EXIST),
            path);
    }

    private void filterAndApplyServerVariablesOnRequestData(
        HttpRequestData httpRequestData, Swagger swagger, Operation operation, HttpHeaders headers,
        Map<String, Object> queryParameters, Map<String, String> pathParameters) {
        for (Parameter parameter : operation.getParameters()) {
            if (parameter instanceof RefParameter) {
                parameter = swagger.getParameter(((RefParameter) parameter).getSimpleRef());
            }
            String paramName = parameter.getName();
            String type = parameter.getIn().toUpperCase();
            Optional<String> variableValue = RestRequestUtils.findVariableValue(parameter);
            if (ParameterType.HEADER.name().equals(type)) {
                List<String> headerValues = variableValue.map(Collections::singletonList)
                    .orElse(httpRequestData.getHttpHeaders().get(paramName));
                if (headerValues != null) {
                    headers.put(paramName, headerValues);
                }
            } else if (ParameterType.QUERY.name().equals(type)) {
                List<String> paramValues = variableValue.map(Collections::singletonList)
                    .orElse(httpRequestData.getQueryParametersMap().get(paramName));
                if (paramValues != null) {
                    queryParameters.put(paramName, paramValues);
                }
            } else if (ParameterType.PATH.name().equals(type)) {
                String pathVariableValue = variableValue
                    .orElse(httpRequestData.getPathVariablesMap().get(paramName));
                if (pathVariableValue != null) {
                    pathParameters.put(paramName, pathVariableValue);
                }
            }
        }
    }

    private void updateAuthorizationInfo(String serviceId, Map<String, SecuritySchemeDefinition> securitySchemeDefinitionMap, Operation operation, Map<String, Object> queryParameters, HttpHeaders httpHeaders, HttpRequestData httpRequestData) {
        //check basic auth is there for operation
        List<Map<String, List<String>>> securityMap = operation.getSecurity();
        if (securityMap != null) {
            for (Map<String, List<String>> securityList : securityMap) {
                for (Map.Entry<String, List<String>> security : securityList.entrySet()) {
                    //TODO update the code to handle if multiple securityConfigurations are enabled for the api.
                    SecuritySchemeDefinition securitySchemeDefinition = securitySchemeDefinitionMap.get(security.getKey());
                    if (securitySchemeDefinition instanceof OAuth2Definition) {
                        OAuth2Definition oAuth2Definition = (OAuth2Definition) securitySchemeDefinition;
                        if (ParameterType.QUERY.name().equalsIgnoreCase(oAuth2Definition.getSendAccessTokenAs())) {
                            queryParameters.put(oAuth2Definition.getAccessTokenParamName(), httpRequestData.getQueryParametersMap().getFirst(oAuth2Definition
                                .getAccessTokenParamName()));
                        } else {
                            sendAsAuthorizationHeader(httpHeaders, httpRequestData);
                        }
                    } else if (securitySchemeDefinition instanceof BasicAuthDefinition) {
                        sendAsAuthorizationHeader(httpHeaders, httpRequestData);
                    } else if (securitySchemeDefinition instanceof ApiKeyAuthDefinition) {
                        ApiKeyAuthDefinition apiKeyAuthDefinition = (ApiKeyAuthDefinition) securitySchemeDefinition;
                        String apiKeyName = apiKeyAuthDefinition.getName();
                        String sanitizedApiKeyName = Arrays.stream(apiKeyName.split("\\W+")).collect(Collectors.joining());
                        if (ParameterType.QUERY.name().equalsIgnoreCase(apiKeyAuthDefinition.getIn().toString())) {
                            String value = getPropertyValue(serviceId, "apikey.query." + sanitizedApiKeyName);
                            if (value != null) {
                                queryParameters.put(apiKeyAuthDefinition.getName(), value);
                            }
                        }
                        if (ParameterType.HEADER.name().equalsIgnoreCase(apiKeyAuthDefinition.getIn().toString())) {
                            String value = getPropertyValue(serviceId, "apikey.header." + sanitizedApiKeyName);
                            if (value != null) {
                                httpHeaders.set(apiKeyAuthDefinition.getName(), value);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendAsAuthorizationHeader(HttpHeaders httpHeaders, HttpRequestData httpRequestData) {
        String authorizationHeaderValue = httpRequestData.getHttpHeaders().getFirst(AUTHORIZATION);
        if (authorizationHeaderValue == null) {
            throw new UnAuthorizedResourceAccessException("Authorization details are not specified in the request headers");
        }
        httpHeaders.set(RestConstants.AUTHORIZATION, authorizationHeaderValue);
    }

    private String getNormalizedString(String str) {
        return (str != null) ? str.trim() : "";
    }

    private String getPropertyValue(String serviceId, String key) {
        String fullKey = "rest." + serviceId + "." + key;
        return environment.getProperty(fullKey);
    }
}

