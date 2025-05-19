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

package com.wavemaker.runtime.web.filter;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.web.matcher.RequestMatcherConfig;
import com.wavemaker.runtime.web.wrapper.BaseHrefReplacementServletResponseWrapper;

public class BaseHrefReplacementFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(BaseHrefReplacementFilter.class);

    private static final String BASE_HREF_PLACEHOLDER = "_baseHref_";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (RequestMatcherConfig.matchesIndexAndPageRequest(httpServletRequest)) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            BaseHrefReplacementServletResponseWrapper baseHrefReplacementServletResponseWrapper = new BaseHrefReplacementServletResponseWrapper(httpServletResponse);
            chain.doFilter(httpServletRequest, baseHrefReplacementServletResponseWrapper);
            String appPath = HttpRequestUtils.getApplicationBaseUrl((HttpServletRequest) request);
            appPath = StringUtils.appendIfMissing(appPath, "/");
            String responseContent = new String(baseHrefReplacementServletResponseWrapper.getByteArray());
            responseContent = responseContent.replace(BASE_HREF_PLACEHOLDER, appPath);
            logger.debug("Replacing _baseHref_ placeholder with the value : {}", appPath);
            httpServletResponse.setContentLengthLong(responseContent.getBytes().length);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(responseContent);
            writer.flush();
            return;
        }
        chain.doFilter(request, response);
    }
}


