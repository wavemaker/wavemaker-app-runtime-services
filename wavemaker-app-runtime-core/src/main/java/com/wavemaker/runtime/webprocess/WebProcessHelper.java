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
package com.wavemaker.runtime.webprocess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.wavemaker.runtime.commons.WMObjectMapper;
import com.wavemaker.runtime.webprocess.model.WebProcess;

public class WebProcessHelper {
    public static final String WEB_PROCESS_COOKIE_NAME = "WM_WEB_PROCESS";
    public static final String WEB_PROCESS_OUTPUT = "WEB_PROCESS_OUTPUT";
    public static final String UTF_8 = StandardCharsets.UTF_8.toString();
    private static final int WEB_PROCESS_COOKIE_MAX_AGE = 10 * 60 * 1000;

    private WebProcessHelper() {
    }

    public static Cookie getCookie(Cookie[] cookies, String cookieName) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static void addWebProcessCookie(HttpServletRequest request, HttpServletResponse response, String webProcessJSON) {
        Cookie cookie = new Cookie(WEB_PROCESS_COOKIE_NAME, webProcessJSON);
        cookie.setPath(request.getServletContext().getContextPath());
        cookie.setMaxAge(WEB_PROCESS_COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        response.addCookie(cookie);
    }

    public static void removeWebProcessCookie(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (WebProcessHelper.WEB_PROCESS_COOKIE_NAME.equalsIgnoreCase(c.getName())) {
                    c.setMaxAge(0);
                    c.setValue(null);
                    c.setPath(request.getServletContext().getContextPath());
                    response.addCookie(c);
                }
            }
        }
    }

    public static WebProcess decodeWebProcess(String process) throws IOException {
        process = new String(Base64.getDecoder().decode(process.getBytes(UTF_8)), UTF_8);
        return WMObjectMapper.getInstance().readValue(process, WebProcess.class);
    }

    public static String encodeWebProcess(WebProcess process) throws IOException {
        String webProcessJSON = WMObjectMapper.getInstance().writeValueAsString(process);
        return Base64.getEncoder().encodeToString(webProcessJSON.getBytes(UTF_8));
    }
}
