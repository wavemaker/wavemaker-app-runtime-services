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
package com.wavemaker.runtime.util;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.Test;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;


public class HttpRequestUtilTest {
    private static final String CONTEXT_PATH = "HelloServlet";
    private static final String COM_WM_EMPTY_OBJECT = "com.wavemaker.runtime.empty.object";
    private final String INPUTSTREAM_PATH = "/com/wavemaker/runtime/util/HttpRequestBody";
    private final String REQUEST_URL = "http://localhost:8080/HelloServlet/login";

    @Test
    public void addCsrfCookieTest() {

        final HttpServletRequest request = getHttpServletRequest();

        final HttpServletResponse response = mock(HttpServletResponse.class);

        CsrfToken defaultCsrfToken = new DefaultCsrfToken("cookie", "_ga", "GA1.2.2072376018.1536596681");
        Optional<CsrfToken> csrfTokenOptional = Optional.ofNullable(defaultCsrfToken);
        HttpRequestUtils.addCsrfCookie(csrfTokenOptional, request, response); //with contextPath
        when(request.getContextPath()).thenReturn("");
        HttpRequestUtils.addCsrfCookie(csrfTokenOptional, request, response); //without ContextPath
    }


    @Test
    public void messageParseTest() {
        assertNotNull(HttpRequestUtils.getFormMessage(getDetailMap()));
        assertNotNull(HttpRequestUtils.getMultipartMessage(getMultiValueMapMap()));
    }


    @Test
    public void jsonMessageTest() {
        try {
            HttpRequestUtils.getJsonMessage("");
            Assert.fail("Should throw exception");
        } catch (WMRuntimeException e) {
            Assert.assertEquals(e.getMessageResourceHolder().getMessageResource().getMessageKey(),
                    COM_WM_EMPTY_OBJECT);
        }
        try {
            assertNotNull(HttpRequestUtils.getJsonMessage("name"));
        } catch (WMRuntimeException e) {
            Assert.assertEquals(e.getMessageResourceHolder().getMessageResource().getMessageKey(),
                    COM_WM_EMPTY_OBJECT);
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[0]);
        when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL));
        when(request.getContextPath()).thenReturn(CONTEXT_PATH);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        return request;
    }

    private static ServletOutputStream getServletOutputStream() {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) throws IOException {
            }
        };
    }

    private HttpResponseDetails getHttpResponseDetails(HttpServletResponse response) {
        HttpResponseDetails httpResponseDetails = new HttpResponseDetails();
        httpResponseDetails.setStatusCode(200);
        httpResponseDetails.setHeaders(getHttpHeaders());
        InputStream resourceAsStream = this.getClass().getResourceAsStream(INPUTSTREAM_PATH);
        httpResponseDetails.setBody(resourceAsStream);
        return httpResponseDetails;
    }

    private static HttpHeaders getHttpHeaders() {
        List<String> listHttp = new ArrayList<>();
        listHttp.add("a");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("a1", "a1");
        httpHeaders.put("MapHttp", listHttp);
        return httpHeaders;
    }

    private static Map<String, Object> getDetailMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("AGE", "Age");
        map.put("COOKIE", "COOKIE");
        return map;
    }

    private static MultiValueMap getMultiValueMapMap() {
        MultiValueMap<String, Object> mMap = new LinkedMultiValueMap<>();
        List<Object> listHttp = new ArrayList<>();
        listHttp.add("a");
        mMap.put("key", listHttp);
        return mMap;
    }
}
