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

package com.wavemaker.runtime.web.matcher;

import java.util.List;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public final class RequestMatcherConfig {

    private RequestMatcherConfig() {
    }

    private static final List<AntPathRequestMatcher> INDEX_PAGE_MATCHERS = List.of(
        new AntPathRequestMatcher("/index.html"),
        new AntPathRequestMatcher("/")
    );

    private static final List<AntPathRequestMatcher> PAGE_MATCHERS = List.of(
        new AntPathRequestMatcher("/page/**")
    );

    public static boolean matchesPageRequest(HttpServletRequest request) {
        return PAGE_MATCHERS.stream().anyMatch(matcher -> matcher.matches(request));
    }

    public static boolean matchesIndexHtmlRequest(HttpServletRequest request) {
        return INDEX_PAGE_MATCHERS.stream().anyMatch(matcher -> matcher.matches(request));
    }

    public static boolean matchesIndexAndPageRequest(HttpServletRequest request) {
        return Stream.concat(INDEX_PAGE_MATCHERS.stream(), PAGE_MATCHERS.stream())
            .anyMatch(matcher -> matcher.matches(request));
    }

}