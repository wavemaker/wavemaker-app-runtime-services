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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.io.NoCloseInputStream;
import com.wavemaker.runtime.rest.model.HttpRequestData;

/**
 * @author Uday Shankar
 */
public class RequestDataBuilder {

    public HttpRequestData getRequestData(HttpServletRequest httpServletRequest) throws IOException {
        HttpRequestData httpRequestData = new HttpRequestData();
        httpRequestData.setHttpHeaders(getHttpHeaders(httpServletRequest));
        httpRequestData.setQueryParametersMap(getQueryParameters(httpServletRequest));
        httpRequestData.setPathVariablesMap(getPathVariablesMap(httpServletRequest));
        httpRequestData.setRequestBody(getRequestBody(httpServletRequest));
        return httpRequestData;
    }

    private HttpHeaders getHttpHeaders(HttpServletRequest httpServletRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            Enumeration<String> headersEnumeration = httpServletRequest.getHeaders(headerName);
            while (headersEnumeration.hasMoreElements()) {
                httpHeaders.add(headerName, headersEnumeration.nextElement());
            }
        }
        return httpHeaders;
    }

    private MultiValueMap<String, String> getQueryParameters(HttpServletRequest httpServletRequest) {
        Charset charset = StandardCharsets.UTF_8;
        String fullUrl = getFullURL(httpServletRequest);
        MultiValueMap<String, String> queryParams;
        try {
            queryParams = UriComponentsBuilder.fromUri(new URI(fullUrl)).build().getQueryParams();
        } catch (URISyntaxException e) {
            throw new WMRuntimeException(e);
        }
        MultiValueMap<String, String> decodedQueryParams = new LinkedMultiValueMap<>();
        for (Map.Entry<String, List<String>> queryParam : queryParams.entrySet()) {
            List<String> decodedQueryParamValues = queryParam.getValue().stream().map(value -> URLDecoder.decode(value, charset)).toList();
            decodedQueryParams.put(URLDecoder.decode(queryParam.getKey(), charset), decodedQueryParamValues);
        }
        return decodedQueryParams;
    }

    private Map<String, String> getPathVariablesMap(HttpServletRequest httpServletRequest) {
        return (Map<String, String>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    private InputStream getRequestBody(HttpServletRequest httpServletRequest) throws IOException {
        return new NoCloseInputStream(httpServletRequest.getInputStream());
    }
}
