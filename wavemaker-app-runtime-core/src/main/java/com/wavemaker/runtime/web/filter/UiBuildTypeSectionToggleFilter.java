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
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.wavemaker.runtime.web.wrapper.ReactPreviewServletResponseWrapper;

public class UiBuildTypeSectionToggleFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(UiBuildTypeSectionToggleFilter.class);

    private static final String UI_BUILD_TYPE_ANGULAR = "angular";
    private static final String UI_BUILD_TYPE_REACT = "react";

    // Captures everything between <!-- Angular Start --> and <!-- Angular end --> (case-insensitive)
    private static final Pattern ANGULAR_SECTION_PATTERN = Pattern.compile(
        "(?is)(<!--\\s*Angular Start\\s*-->)(.*?)(<!--\\s*Angular end\\s*-->)");

    // Captures everything between <!-- React Start --> and <!-- React end --> (case-insensitive)
    private static final Pattern REACT_SECTION_PATTERN = Pattern.compile(
        "(?is)(<!--\\s*React Start\\s*-->)(.*?)(<!--\\s*React end\\s*-->)");

    // Matches each commented block: <!-- content --> ((?s) allows content to span multiple lines)
    private static final Pattern INNER_COMMENT_PATTERN = Pattern.compile("(?s)<!--\\s*(.+?)\\s*-->");

    // Matches non-empty, non-comment lines within a section (for commenting back)
    private static final Pattern INNER_ACTIVE_LINE_PATTERN = Pattern.compile(
        "(?m)^(\\s*+)(?!<!--)(\\S[^\\n]*)$");

    private String uiBuildType;

    private final AntPathRequestMatcher rootPathMatcher = new AntPathRequestMatcher("/");
    private final AntPathRequestMatcher indexPathMatcher = new AntPathRequestMatcher("/index.html");

    @Override
    protected void initFilterBean() {
        uiBuildType = getServletContext().getInitParameter("uiBuildType");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isIndexRequest(httpRequest) && isKnownBuildType(uiBuildType)) {
            logger.debug("Uncommenting blocks in index.html for uiBuildType={}", uiBuildType);
            ReactPreviewServletResponseWrapper wrapper = new ReactPreviewServletResponseWrapper(httpResponse);
            chain.doFilter(request, wrapper);
            String content = new String(wrapper.getByteArray(), StandardCharsets.UTF_8);
            content = uncommentBlocksForBuildType(content, uiBuildType);
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            httpResponse.setContentLengthLong(bytes.length);
            httpResponse.getOutputStream().write(bytes);
            httpResponse.getOutputStream().flush();
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isIndexRequest(HttpServletRequest request) {
        return rootPathMatcher.matches(request) || indexPathMatcher.matches(request);
    }

    private boolean isKnownBuildType(String uiBuildType) {
        return UI_BUILD_TYPE_ANGULAR.equals(uiBuildType) || UI_BUILD_TYPE_REACT.equals(uiBuildType);
    }

    private String uncommentBlocksForBuildType(String content, String uiBuildType) {
        if (UI_BUILD_TYPE_REACT.equals(uiBuildType)) {
            content = uncommentSection(content, REACT_SECTION_PATTERN);
            content = commentSection(content, ANGULAR_SECTION_PATTERN);
        } else {
            content = uncommentSection(content, ANGULAR_SECTION_PATTERN);
            content = commentSection(content, REACT_SECTION_PATTERN);
        }
        return content;
    }

    private String uncommentSection(String content, Pattern sectionPattern) {
        Matcher sectionMatcher = sectionPattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (sectionMatcher.find()) {
            String inner = INNER_COMMENT_PATTERN.matcher(sectionMatcher.group(2)).replaceAll("$1");
            sectionMatcher.appendReplacement(sb,
                Matcher.quoteReplacement(sectionMatcher.group(1) + inner + sectionMatcher.group(3)));
        }
        sectionMatcher.appendTail(sb);
        return sb.toString();
    }

    private String commentSection(String content, Pattern sectionPattern) {
        Matcher sectionMatcher = sectionPattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (sectionMatcher.find()) {
            String inner = INNER_COMMENT_PATTERN.matcher(sectionMatcher.group(2)).replaceAll("$1");
            inner = INNER_ACTIVE_LINE_PATTERN.matcher(inner).replaceAll("$1<!-- $2 -->");
            sectionMatcher.appendReplacement(sb,
                Matcher.quoteReplacement(sectionMatcher.group(1) + inner + sectionMatcher.group(3)));
        }
        sectionMatcher.appendTail(sb);
        return sb.toString();
    }
}
