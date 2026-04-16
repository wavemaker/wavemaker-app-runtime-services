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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UrlPathHelper;

public class ReactPreviewFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(ReactPreviewFilter.class);

    private static final String INDEX_HTML_PATH = "/index.html";
    private static final String REACT_BUILD_MODE = "react";
    private static final String BASE_HREF_REGEX = "<base\\s+href=\"[^\"]*\"";

    private final AntPathRequestMatcher reactPreviewPathMatcher = new AntPathRequestMatcher("/react-preview/**");
    private final AntPathRequestMatcher rootPathMatcher = new AntPathRequestMatcher("/");
    private final AntPathRequestMatcher indexPathMatcher = new AntPathRequestMatcher("/index.html");

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (requestMatches(httpRequest)) {
            String html = buildIndexHtml(httpRequest.getContextPath());
            if (html != null) {
                logger.debug("Serving index.html for react-preview request: {}", urlPathHelper.getPathWithinApplication(httpRequest));
                writeHtmlResponse((HttpServletResponse) response, html);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean requestMatches(HttpServletRequest request) {
        String uiBuildType = getServletContext().getInitParameter("uiBuildType");
        return REACT_BUILD_MODE.equals(uiBuildType) && Stream.of(reactPreviewPathMatcher, rootPathMatcher, indexPathMatcher)
            .anyMatch(antPathRequestMatcher -> antPathRequestMatcher.matches(request));
    }

    private String buildIndexHtml(String contextPath) throws IOException {
        try (InputStream inputStream = getServletContext().getResourceAsStream(INDEX_HTML_PATH)) {
            if (inputStream == null) {
                logger.warn("index.html not found in servlet context; falling through for react-preview requests");
                return null;
            }
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String replacement = "<base href=\"" + contextPath + "/\"";
            content = content.replaceAll(BASE_HREF_REGEX, replacement);
            logger.debug("Patched <base href> in index.html to '{}'", contextPath + "/");
            return content;
        }
    }

    private void writeHtmlResponse(HttpServletResponse response, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/html;charset=UTF-8");
        response.setContentLengthLong(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}