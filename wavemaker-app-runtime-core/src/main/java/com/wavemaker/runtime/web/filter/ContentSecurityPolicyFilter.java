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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.runtime.web.matcher.RequestMatcherConfig;

public class ContentSecurityPolicyFilter extends GenericFilterBean {

    @Value("${security.general.csp.enabled}")
    private boolean cspEnabled;

    @Value("${security.general.csp.policy}")
    private String cspPolicy;

    public static final int LENGTH = 12;
    private boolean nonceReplacementNeeded;

    private static final String NONCE_VALUE_PATTERN = "\\$\\{NONCE_VALUE\\}";
    private static final String NONCE_PLACEHOLDER = "${NONCE_VALUE}";
    private static final String CSP_HEADER = "Content-Security-Policy";
    private static final Logger logger = LoggerFactory.getLogger(ContentSecurityPolicyFilter.class);

    @Override
    protected void initFilterBean() {
        if (cspEnabled) {
            if (StringUtils.isNotBlank(cspPolicy)) {
                nonceReplacementNeeded = cspPolicy.contains(NONCE_PLACEHOLDER);
                logger.info("csp policy is {} and nonceReplacementNeeded:{}", cspPolicy, nonceReplacementNeeded);
            } else {
                logger.warn("Disabling csp as cspPolicy is blank");
                cspEnabled = false;
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (cspEnabled && RequestMatcherConfig.matchesIndexHtmlRequest(httpServletRequest)) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            if (nonceReplacementNeeded) {
                String nonce = generateRandomNonce();
                httpServletResponse.addHeader(CSP_HEADER, cspPolicy.replace(NONCE_PLACEHOLDER, nonce));
                CSPResponseWrapper cspResponseWrapper = new CSPResponseWrapper(httpServletResponse);
                chain.doFilter(httpServletRequest, cspResponseWrapper);
                String res = new String(cspResponseWrapper.getByteArray());
                res = res.replaceAll(NONCE_VALUE_PATTERN, nonce);
                httpServletResponse.setContentLengthLong(res.getBytes().length);
                PrintWriter writer = httpServletResponse.getWriter();
                writer.write(res);
                writer.flush();
                return;
            } else {
                httpServletResponse.addHeader(CSP_HEADER, cspPolicy);
            }
        }
        chain.doFilter(request, response);
    }

    private String generateRandomNonce() {
        return RandomStringUtils.secureStrong().nextAlphanumeric(LENGTH);
    }
}
