/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.security.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.context.HttpRequestResponseHolder;

public class WMRequestResponseHolderFilter implements Filter {

    private static ThreadLocal<HttpRequestResponseHolder> httpRequestResponseHolderThreadLocal = new ThreadLocal<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            httpRequestResponseHolderThreadLocal.set(new HttpRequestResponseHolder(httpServletRequest, httpServletResponse));
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            httpRequestResponseHolderThreadLocal.remove();
        }
    }

    public static HttpServletRequest getCurrentThreadHttpServletRequest() {
        HttpRequestResponseHolder httpRequestResponseHolder = httpRequestResponseHolderThreadLocal.get();
        if (httpRequestResponseHolder != null) {
            return httpRequestResponseHolder.getRequest();
        }
        return null;
    }

    public static HttpServletResponse getCurrentThreadHttpServletResponse() {
        HttpRequestResponseHolder httpRequestResponseHolder = httpRequestResponseHolderThreadLocal.get();
        if (httpRequestResponseHolder != null) {
            return httpRequestResponseHolder.getResponse();
        }
        return null;
    }
}
