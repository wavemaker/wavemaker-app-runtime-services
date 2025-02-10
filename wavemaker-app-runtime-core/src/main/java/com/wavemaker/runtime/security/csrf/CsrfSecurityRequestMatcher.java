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
package com.wavemaker.runtime.security.csrf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.wavemaker.app.security.models.CSRFConfig;

/**
 * Created by kishorer on 25/8/15.
 */
public class CsrfSecurityRequestMatcher implements RequestMatcher {

    @Autowired
    private Environment environment;

    private Set<String> unprotectedHttpMethods;
    private List<RegexRequestMatcher> unprotectedMatchers;

    private CSRFConfig csrfConfig;

    @PostConstruct
    public void init() {
        String unprotectedMatchersStr = environment.getProperty("security.general.xsrf.unprotectedUrlMatchers");
        String unprotectedMethodsStr = environment.getProperty("security.general.xsrf.unprotectedHttpMethods", "GET,HEAD,TRACE,OPTIONS");
        List<String> unprotectedMatchersList = new ArrayList<>();
        if (StringUtils.isNotBlank(unprotectedMatchersStr)) {
            unprotectedMatchersList = Arrays.stream(unprotectedMatchersStr.split(",")).toList();
        }
        unprotectedHttpMethods = Arrays.stream(unprotectedMethodsStr.split(",")).collect(Collectors.toSet());
        unprotectedMatchers = new ArrayList<>(unprotectedMatchersList.size());
        for (String pattern : unprotectedMatchersList) {
            unprotectedMatchers.add(new RegexRequestMatcher(pattern, null));
        }
    }

    public void setCsrfConfig(final CSRFConfig csrfConfig) {
        this.csrfConfig = csrfConfig;
    }

    @Override
    public boolean matches(HttpServletRequest httpServletRequest) {
        if (csrfConfig == null || !csrfConfig.isEnforceCsrfSecurity()) {
            return false;
        }
        if (unprotectedHttpMethods.contains(httpServletRequest.getMethod())) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        if (unprotectedMatchers != null && !unprotectedMatchers.isEmpty()) {
            for (RegexRequestMatcher unprotectedMatcher : unprotectedMatchers) {
                if (unprotectedMatcher != null && unprotectedMatcher.matches(httpServletRequest)) {
                    return false;
                }
            }
        }
        return true;
    }

}
