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
package com.wavemaker.runtime.security.xss.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.runtime.security.xss.XSSRequestWrapper;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

/**
 * Filter implementation to add header X-XSS-Protection and XSS encode filter.
 */
public class WMXSSFilter extends GenericFilterBean {

    private XXssProtectionHeaderWriter xXssProtectionHeaderWriter;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();

        xXssProtectionHeaderWriter = new XXssProtectionHeaderWriter();
        xXssProtectionHeaderWriter.setHeaderValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK);
        xXssProtectionHeaderWriter.setHeaderValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        xXssProtectionHeaderWriter.writeHeaders(httpServletRequest, httpServletResponse);
        XSSSecurityHandler xssSecurityHandler = XSSSecurityHandler.getInstance();
        if (xssSecurityHandler.isInputSanitizationEnabled()) {
            ServletRequestWrapper servletRequestWrapper = new XSSRequestWrapper(httpServletRequest, xssSecurityHandler);
            chain.doFilter(servletRequestWrapper, httpServletResponse);
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {
    }
}
