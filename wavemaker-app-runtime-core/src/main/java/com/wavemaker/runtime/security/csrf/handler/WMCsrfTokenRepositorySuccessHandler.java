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
package com.wavemaker.runtime.security.csrf.handler;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.app.security.models.CSRFConfig;

/**
 * Created by srujant on 31/10/18.
 */
public class WMCsrfTokenRepositorySuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(WMCsrfTokenRepositorySuccessHandler.class);

    private CsrfTokenRepository csrfTokenRepository;

    @Value("#{${security.general.cookie.maxAge:-1} * 60}")
    private int cookieMaxAge;

    @Value("${security.general.cookie.path}")
    private String cookiePath;

    @Value("${security.general.cookie.sameSite}")
    private String cookieSameSite;

    public WMCsrfTokenRepositorySuccessHandler(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        CSRFConfig csrfConfig = WMAppContext.getInstance().getSpringBean(CSRFConfig.class);
        Optional<CsrfToken> csrfTokenOptional = getCsrfToken(request, csrfConfig);
        if (csrfTokenOptional.isPresent()) {
            addCsrfCookie(csrfTokenOptional, request, response, csrfConfig);
            csrfTokenRepository.saveToken(csrfTokenOptional.get(), request, response);
        }
    }

    private Optional<CsrfToken> getCsrfToken(HttpServletRequest request, CSRFConfig csrfConfig) {
        if (csrfConfig != null && csrfConfig.isEnforceCsrfSecurity()) {
            CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
            return Optional.ofNullable(csrfToken);
        }
        return Optional.empty();
    }

    private void addCsrfCookie(Optional<CsrfToken> csrfTokenOptional, HttpServletRequest request, HttpServletResponse response, CSRFConfig csrfConfig) {
        logger.info("Adding CsrfCookie");
        if (csrfTokenOptional.isPresent()) {
            CsrfToken csrfToken = csrfTokenOptional.get();
            ResponseCookie.ResponseCookieBuilder wmXsrfCookieBuilder = ResponseCookie.from(csrfConfig.getCookieName(), csrfToken.getToken());
            String path;
            String contextPath = request.getContextPath();
            if (StringUtils.isNotBlank(this.cookiePath)) {
                path = this.cookiePath;
            } else if (StringUtils.isNotBlank(contextPath)) {
                path = contextPath;
            } else {
                path = "/";
            }
            wmXsrfCookieBuilder
                .path(path)
                .secure(request.isSecure())
                .maxAge(cookieMaxAge);
            if (StringUtils.isNotBlank(cookieSameSite)) {
                wmXsrfCookieBuilder.sameSite(cookieSameSite);
            }
            response.addHeader(HttpHeaders.SET_COOKIE, wmXsrfCookieBuilder.build().toString());
        }
    }

    public void setCsrfTokenRepository(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

}
