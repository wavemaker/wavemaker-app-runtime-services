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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.commons.web.filter.EtagFilter;
import com.wavemaker.runtime.web.SkipEtagHttpServletResponseWrapper;

/**
 * Created by srujant on 27/3/19.
 */
public class CacheManagementFilter extends GenericFilterBean {

    private static final RequestMatcher cacheRequestMatcher = new AntPathRequestMatcher("/ng-bundle/**", HttpMethod.GET.name());

    private static final RequestMatcher cacheExclusionRequestMatcher = new AntPathRequestMatcher("/ng-bundle/path_mapping.json", HttpMethod.GET.name());

    private static final RequestMatcher etagRequestMatcher = new AntPathRequestMatcher("/**", HttpMethod.GET.name());

    @Autowired
    private EtagFilter etagFilter;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (cacheRequestMatcher.matches(httpServletRequest) && !cacheExclusionRequestMatcher.matches(httpServletRequest)) {
            httpServletResponse.addHeader("Cache-Control", "public, max-age=31536000");
            chain.doFilter(request, new SkipEtagHttpServletResponseWrapper(httpServletResponse));
        } else if (etagRequestMatcher.matches(httpServletRequest)) {
            etagFilter.doFilter(request, response, chain);
        } else {
            httpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            httpServletResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
            httpServletResponse.setDateHeader("Expires", 0);
            chain.doFilter(request, response);
        }
    }
}