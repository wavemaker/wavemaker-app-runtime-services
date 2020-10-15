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

import com.wavemaker.runtime.rest.model.HttpResponseDetails;
import com.wavemaker.runtime.rest.util.HttpResponseUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class HttpResponseUtilTest {
    private static final String SET_COOKIE_HEADER = "Set-Cookie";

    @Test
    public void getCookiesTest() {
        assertNotNull(HttpResponseUtils.getCookies(getHttpResponseDetails()));
    }

    @Test
    public void setCookiesTest() {
        HttpResponseUtils.setCookies(getHttpResponseDetails(), getHttpCookieList());
    }

    @Test
    public void toStringWithoutParametersTest() {
        MediaType mediaType = new MediaType("NAME", "VALUE");
        assertNotNull(HttpResponseUtils.toStringWithoutParameters(mediaType));
    }

    private HttpResponseDetails getHttpResponseDetails() {
        HttpResponseDetails httpResponseDetails = new HttpResponseDetails();
        httpResponseDetails.setStatusCode(200);
        httpResponseDetails.setHeaders(getHttpHeaders());
        return httpResponseDetails;
    }

    private static HttpHeaders getHttpHeaders() {
        List<String> listHttp = new ArrayList<>();
        listHttp.add("Cookie=Value121");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("a1", "a1");
        httpHeaders.put(SET_COOKIE_HEADER, listHttp);
        return httpHeaders;
    }

    private static List<HttpCookie> getHttpCookieList() {
        HttpCookie httpCookie = new HttpCookie("id", "11111");
        httpCookie.setPath("/Wavemaker/test");
        httpCookie.setMaxAge(1559685475);
        httpCookie.setDiscard(true);
        List<HttpCookie> httpCookiesList = new ArrayList<>();
        httpCookiesList.add(httpCookie);
        return httpCookiesList;
    }
}
