/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.rest.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;

import com.wavemaker.commons.proxy.AppPropertiesConstants;
import com.wavemaker.commons.rest.error.WMDefaultResponseErrorHandler;
import com.wavemaker.commons.util.SSLUtils;
import com.wavemaker.runtime.AppRuntimeProperties;
import com.wavemaker.runtime.rest.model.HttpRequestDetails;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;

/**
 * @author Uday Shankar
 */

public class RestConnector {

    private static final boolean USE_SYSTEM_PROPERTIES = Boolean.getBoolean("app.rest.useSystemProperties");
    private static final int MAX_TOTAL = Integer.getInteger("app.rest.maxTotalConnections", 100);
    private static final int DEFAULT_MAX_PER_ROUTE = Integer.getInteger("app.rest.maxConnectionsPerRoute", 50);
    private static final Logger logger = LoggerFactory.getLogger(RestConnector.class);
    private static CloseableHttpClient defaultHttpClient;

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
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setSocketTimeout((int) TimeUnit.MINUTES.toMillis(6))// 6 mins, something more than lb time out  
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30))
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(5))
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
        com.wavemaker.commons.web.http.HttpMethod wmHttpMethod = com.wavemaker.commons.web.http.HttpMethod.valueOf(httpRequestDetails.getMethod());
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
                        .setDefaultCredentialsProvider(getCredentialProvider())
                        .setConnectionManager(getConnectionManager());
                if (USE_SYSTEM_PROPERTIES) {
                    httpClientBuilder = httpClientBuilder.useSystemProperties();
                }
                defaultHttpClient = httpClientBuilder.build();
            }
        }

        return defaultHttpClient;
    }

    private CredentialsProvider getCredentialProvider() {

        boolean isEnabled = Boolean.valueOf(AppRuntimeProperties.getProperty(AppPropertiesConstants.APP_PROXY_ENABLED));
        CredentialsProvider credentialsProvider = null;
        if (isEnabled) {
            credentialsProvider = new BasicCredentialsProvider();
            String hostName = AppRuntimeProperties.getProperty(AppPropertiesConstants.APP_PROXY_HOST);
            String port = AppRuntimeProperties.getProperty(AppPropertiesConstants.APP_PROXY_PORT);
            String userName = AppRuntimeProperties.getProperty(AppPropertiesConstants.APP_PROXY_USERNAME);
            if (userName == null) {
                userName = "";
            }
            String passWord = AppRuntimeProperties.getProperty(AppPropertiesConstants.APP_PROXY_PASSWORD);
            int proxyPort = 0;
            if (port != null && !("".equals(port))) {
                proxyPort = Integer.parseInt(port);
            }
            credentialsProvider.setCredentials(new AuthScope(hostName, proxyPort), new UsernamePasswordCredentials(userName, passWord));
        }
        return credentialsProvider;
    }

    private PoolingHttpClientConnectionManager getConnectionManager() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", new SSLConnectionSocketFactory(SSLUtils.getAllTrustedCertificateSSLContext(), new String[]{"TLSv1.2", "TLSv1.1", "TLSv1"}, null, NoopHostnameVerifier.INSTANCE))
                .build();

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        poolingHttpClientConnectionManager.setMaxTotal(MAX_TOTAL);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
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

    class WMRestServicesErrorHandler extends WMDefaultResponseErrorHandler {

        @Override
        protected boolean hasError(HttpStatus statusCode) {
            return false;
        }
    }
}
