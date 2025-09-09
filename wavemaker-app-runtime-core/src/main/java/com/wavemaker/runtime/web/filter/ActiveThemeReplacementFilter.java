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

package com.wavemaker.runtime.web.filter;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.runtime.service.AppRuntimeService;
import com.wavemaker.runtime.web.wrapper.ActiveThemeReplacementServletResponseWrapper;

public class ActiveThemeReplacementFilter extends GenericFilterBean {
    private static final String ACTIVE_THEME_PLACEHOLDER = "_activeTheme_";

    private AntPathRequestMatcher indexPathMatcher = new AntPathRequestMatcher("/index.html");
    private AntPathRequestMatcher rootPathMatcher = new AntPathRequestMatcher("/");
    @Autowired
    private AppRuntimeService appRuntimeService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        if (requestMatches(httpServletRequest)) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            ActiveThemeReplacementServletResponseWrapper activeThemeReplacementServletResponseWrapper =
                new ActiveThemeReplacementServletResponseWrapper(httpServletResponse);
            chain.doFilter(httpServletRequest, activeThemeReplacementServletResponseWrapper);
            String response = new String(activeThemeReplacementServletResponseWrapper.getByteArray());
            response = response.replace(ACTIVE_THEME_PLACEHOLDER, appRuntimeService.getActiveTheme());
            httpServletResponse.setContentLengthLong(response.getBytes().length);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(response);
            writer.flush();
            return;
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    protected boolean requestMatches(HttpServletRequest httpServletRequest) {
        return this.indexPathMatcher.matches(httpServletRequest) || this.rootPathMatcher.matches(httpServletRequest);
    }
}
