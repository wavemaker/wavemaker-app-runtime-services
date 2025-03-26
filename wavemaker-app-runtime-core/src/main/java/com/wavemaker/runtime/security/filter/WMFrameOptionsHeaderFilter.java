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
package com.wavemaker.runtime.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.app.security.models.FrameOptions;
import com.wavemaker.commons.util.HttpRequestUtils;

/**
 * Filter implementation to add header X-Frame-Options.
 */
public class WMFrameOptionsHeaderFilter extends GenericFilterBean {

    private static String FRAME_ANCESTORS_HEADER = "frame-ancestors ";

    private FrameOptions frameOptions;

    public WMFrameOptionsHeaderFilter(FrameOptions frameOptions) {
        this.frameOptions = frameOptions;
    }

    private HeaderWriter contentSecurityPolicyHeaderWriter;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        String allowFromUrls = switch (frameOptions.getMode()) {
            case DENY -> "'none'";
            case SAMEORIGIN -> "'self'";
            default -> getWhiteListedUrls();
        };
        contentSecurityPolicyHeaderWriter = new ContentSecurityPolicyHeaderWriter(FRAME_ANCESTORS_HEADER + allowFromUrls);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
        IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (frameOptions.isEnabled() && !HttpRequestUtils.isAjaxRequest(httpServletRequest)) {
            contentSecurityPolicyHeaderWriter.writeHeaders(httpServletRequest, httpServletResponse);
        }
        chain.doFilter(request, response);
    }

    private String getWhiteListedUrls() {
        Set<String> allowFromUrls = new LinkedHashSet<>();
        allowFromUrls.add("'self'");
        if (StringUtils.isNotBlank(frameOptions.getAllowFromUrl())) {
            Arrays.stream(frameOptions.getAllowFromUrl().split(",")).map(String::trim).forEach(domain -> {
                if (domain.endsWith("/")) {
                    domain = domain.substring(0, domain.length() - 1);
                }
                allowFromUrls.add(domain);
            });
        }
        return Strings.join(allowFromUrls, ' ');
    }
}
