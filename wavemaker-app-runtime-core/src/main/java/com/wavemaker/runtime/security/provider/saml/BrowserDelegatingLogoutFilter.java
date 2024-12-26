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
package com.wavemaker.runtime.security.provider.saml;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.commons.wrapper.StringWrapper;
import com.wavemaker.runtime.security.WMAuthentication;
import com.wavemaker.runtime.security.constants.SecurityProviders;

/**
 * Created by ArjunSahasranam on 25/11/16.
 */
public class BrowserDelegatingLogoutFilter extends LogoutFilter {

    private static final Logger logger = LoggerFactory.getLogger(BrowserDelegatingLogoutFilter.class);

    public BrowserDelegatingLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler... handlers) {
        super(logoutSuccessHandler, handlers);
        setFilterProcessesUrl("/j_spring_security_logout");
    }

    @Override
    public void doFilter(
        final ServletRequest req, final ServletResponse res,
        final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (requiresLogout(request, response)) {
            logger.info("Request for logout");
            WMAuthentication wmAuthentication = new WMAuthentication(SecurityContextHolder.getContext().getAuthentication());
            if (HttpRequestUtils.isAjaxRequest(request) &&
                SecurityProviders.getBrowserRedirectLogoutSupportedProviders().anyMatch(securityProviders ->
                    Objects.equals(securityProviders.getProviderType(), wmAuthentication.getProviderType()))) {
                logger.info("Redirecting to the same request uri {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(JSONUtils.toJSON(new StringWrapper(request.getRequestURI())));
                response.getWriter().flush();
            } else {
                super.doFilter(request, response, chain);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
