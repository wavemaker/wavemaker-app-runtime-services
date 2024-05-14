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

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;

import com.wavemaker.commons.rest.error.WMDefaultResponseErrorHandler;
import com.wavemaker.runtime.rest.model.HttpRequestDetails;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;

/**
 * @author Uday Shankar
 */

public class RestConnector {

    private static final Logger logger = LoggerFactory.getLogger(RestConnector.class);

    private CloseableHttpClient defaultHttpClient;
    private HttpConfiguration httpConfiguration;

    public static final RestConnector DEFAULT_INSTANCE = new RestConnector();

    @Deprecated
    public RestConnector() {
        this(new StandardEnvironment());
    }

    @Autowired
    public RestConnector(Environment environment) {
        httpConfiguration = new HttpConfiguration(environment);
        logger.info("Initialized http configuration {}", httpConfiguration);
    }

    public void invokeRestCall(HttpRequestDetails httpRequestDetails, Consumer<ClientHttpResponse> extractDataConsumer) {
        final HttpClientContext httpClientContext = HttpClientContext.create();
        WMRestTemplate wmRestTemplate = new WMRestTemplate();
        wmRestTemplate.setExtractDataConsumer(extractDataConsumer);
        getResponseEntity(httpRequestDetails, httpClientContext, null, wmRestTemplate);
    }

    public HttpResponseDetails invokeRestCall(HttpRequestDetails httpRequestDetails) {
        final HttpClientContext httpClientContext = HttpClientContext.create();
        ResponseEntity<byte[]> responseEntity = getResponseEntity(httpRequestDetails, httpClientContext, byte[].class, new WMRestTemplate());
        return getHttpResponseDetails(responseEntity);
    }

    public <T> ResponseEntity<T> invokeRestCall(HttpRequestDetails httpRequestDetails, Class<T> t) {
        final HttpClientContext httpClientContext = HttpClientContext.create();
        return getResponseEntity(httpRequestDetails, httpClientContext, t, new WMRestTemplate());
    }

    private <T> ResponseEntity<T> getResponseEntity(
        final HttpRequestDetails httpRequestDetails, final HttpClientContext
        httpClientContext, Class<T> t, final WMRestTemplate wmRestTemplate) {

        String endpointAddress = httpRequestDetails.getEndpointAddress();
        HttpMethod httpMethod = HttpMethod.valueOf(httpRequestDetails.getMethod());
        logger.debug("Sending {} request to URL {}", httpMethod, endpointAddress);

        CloseableHttpClient httpClient = getHttpClient();

        final RequestConfig requestConfig = RequestConfig.custom()
            .setRedirectsEnabled(httpRequestDetails.isRedirectEnabled())
            //TODO  socketTimeout is removed in httpclient5 and aCookieSpecs enum is removed
            .setCookieSpec(StandardCookieSpec.IGNORE)
            .setResponseTimeout(Timeout.ofSeconds(httpConfiguration.getConnectionSocketTimeoutInSeconds()))
            .setConnectTimeout(Timeout.ofSeconds(httpConfiguration.getConnectionTimeoutInSeconds()))
            .setConnectionRequestTimeout(Timeout.ofSeconds(httpConfiguration.getConnectionRequestTimeoutInSeconds()))
            .build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient) {
            @Override
            protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
                return httpClientContext;
            }

            @Override
            protected RequestConfig createRequestConfig(Object client) {
                return requestConfig;
            }
        };

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.putAll(httpRequestDetails.getHeaders());

        wmRestTemplate.setRequestFactory(clientHttpRequestFactory);
        wmRestTemplate.setErrorHandler(getExceptionHandler());

        HttpEntity requestEntity;
        com.wavemaker.app.web.http.HttpMethod wmHttpMethod = com.wavemaker.app.web.http.HttpMethod.valueOf(httpRequestDetails.getMethod());
        if (wmHttpMethod.isRequestBodySupported() && httpRequestDetails.getBody() != null) {
            requestEntity = new HttpEntity(new InputStreamResource(httpRequestDetails.getBody()), httpHeaders);
        } else {
            requestEntity = new HttpEntity(httpHeaders);
        }
        return wmRestTemplate.exchange(endpointAddress, httpMethod, requestEntity, t);
    }

    public ResponseErrorHandler getExceptionHandler() {
        return new WMRestServicesErrorHandler();
    }

    private CloseableHttpClient getHttpClient() {
        synchronized (RestConnector.class) {
            if (defaultHttpClient == null) {
                HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .setConnectionManager(getConnectionManager());
                if (httpConfiguration.isUseSystemProperties()) {
                    httpClientBuilder = httpClientBuilder.useSystemProperties();
                }
                configureAppProxy(httpClientBuilder);
                defaultHttpClient = httpClientBuilder.build();
            }
        }
        return defaultHttpClient;
    }

    private void configureAppProxy(HttpClientBuilder httpClientBuilder) {
        if (httpConfiguration.isAppProxyEnabled()) {
            HttpHost proxyHost = new HttpHost(httpConfiguration.getAppProxyHost(), httpConfiguration.getAppProxyPort());
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(proxyHost),
                new UsernamePasswordCredentials(httpConfiguration.getAppProxyUsername(), httpConfiguration.getAppProxyPassword().toCharArray()));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            logger.info("creating RoutePlanner with proxy url {}", proxyHost);
            HttpRoutePlanner httpRoutePlanner = createCustomRoutePlanner(proxyHost,
                parseCommaSeparatedUrlsToList(httpConfiguration.getAppProxyIncludeUrls()),
                parseCommaSeparatedUrlsToList(httpConfiguration.getAppProxyExcludeUrls()));
            httpClientBuilder.setRoutePlanner(httpRoutePlanner);
        }
    }

    private PoolingHttpClientConnectionManager getConnectionManager() {
        SSLContextProvider sslContextProvider = new SSLContextProvider(httpConfiguration);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", new SSLConnectionSocketFactory(sslContextProvider.getSslContext(),
                httpConfiguration.getTlsVersions(), null, sslContextProvider.getHostnameVerifier()))
            .build();

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        poolingHttpClientConnectionManager.setMaxTotal(httpConfiguration.getMaxTotalConnections());
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(httpConfiguration.getMaxTotalConnectionsPerRoute());
        return poolingHttpClientConnectionManager;
    }

    private HttpResponseDetails getHttpResponseDetails(ResponseEntity<byte[]> responseEntity) {
        HttpResponseDetails httpResponseDetails = new HttpResponseDetails();
        httpResponseDetails.setStatusCode(responseEntity.getStatusCode().value());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.putAll(responseEntity.getHeaders());
        httpResponseDetails.setHeaders(httpHeaders);
        byte[] bytes = (responseEntity.getBody() != null) ? responseEntity.getBody() : new byte[0];
        httpResponseDetails.setBody(new ByteArrayInputStream(bytes));
        return httpResponseDetails;
    }

    private HttpRoutePlanner createCustomRoutePlanner(HttpHost proxyHost, List<String> includedUrls, List<String> excludedUrls) {
        DefaultProxyRoutePlanner defaultRoutePlanner = new DefaultProxyRoutePlanner(proxyHost);

        //TODO need to verify in runtime
        return (target, context) -> {
            boolean explicitlyIncluded = includedUrls.stream().anyMatch(url -> matches(target, url));

            boolean useProxy = true;
            if (explicitlyIncluded) {
                logger.debug("Url {} is explicitly included for proxying", target);
            } else if (includedUrls.isEmpty()) {
                boolean explicitlyExcluded = excludedUrls.stream().anyMatch(url -> matches(target, url));
                if (explicitlyExcluded) {
                    useProxy = false;
                    logger.debug("Url {} is explicitly excluded from proxying", target);
                }
            } else {
                useProxy = false;
            }
            if (useProxy) {
                logger.debug("Using proxy {} for target {}", proxyHost, target);
                return defaultRoutePlanner.determineRoute(target, context);
            } else {
                return new HttpRoute(target);
            }
        };
    }

    private boolean matches(HttpHost target, String url) {
        String urlHostName = extractHostName(url);
        int urlPort = extractPort(url);
        String urlScheme = extractScheme(url);
        return Objects.equals(target.getHostName(), urlHostName) && (urlPort == -1 || urlPort == target.getPort()) &&
            (urlScheme == null || StringUtils.equalsIgnoreCase(urlScheme, target.getSchemeName()));
    }

    private List<String> parseCommaSeparatedUrlsToList(String commaSeparatedUrls) {
        return commaSeparatedUrls.trim().isEmpty() ? Collections.emptyList() : Arrays.stream(commaSeparatedUrls.split(","))
            .map(String::trim).filter(url -> !url.isEmpty()).collect(Collectors.toList());
    }

    private String extractHostName(String url) {
        int start = url.indexOf("://");
        if (start != -1) {
            start += 3;
        } else {
            start = 0;
        }
        int pathStart = url.indexOf('/', start);
        int portStart = url.indexOf(':', start);
        int end = (pathStart != -1 && (portStart == -1 || portStart > pathStart)) ? pathStart : portStart;
        if (end == -1) {
            end = url.length();
        }
        return url.substring(start, end);
    }

    private String extractScheme(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getProtocol();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private int extractPort(String url) {
        int portStart = url.lastIndexOf(':');
        int pathStart = url.indexOf('/', portStart);
        if (portStart != -1 && (pathStart == -1 || portStart < pathStart)) {
            String portString = url.substring(portStart + 1, pathStart != -1 ? pathStart : url.length());
            try {
                return Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    static class WMRestServicesErrorHandler extends WMDefaultResponseErrorHandler {

        @Override
        protected boolean hasError(HttpStatusCode statusCode) {
            return false;
        }
    }
}
