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
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.WMAuthentication;

/**
 * Created by srujant on 31/10/18.
 */
public class WMBasicAuthenticationFilter extends BasicAuthenticationFilter {

    private AuthenticationSuccessHandler basicAuthenticationSuccessHandler;

    public WMBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    public WMBasicAuthenticationFilter(AuthenticationManager authenticationManager, AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationManager, authenticationEntryPoint);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            super.doFilterInternal(request, response, chain);
        } finally {
            if (request.getAttribute("basicAuthLogin") != null) {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Override
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
        authResult = new WMAuthentication(authResult);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        if (Objects.nonNull(basicAuthenticationSuccessHandler)) {
            try {
                basicAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);
            } catch (Exception e) {
                SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
                throw new WMRuntimeException(e);
            }
        }
        super.onSuccessfulAuthentication(request, response, authResult);
        request.setAttribute("basicAuthLogin", true);
    }

    public void setBasicAuthenticationSuccessHandler(AuthenticationSuccessHandler basicAuthenticationSuccessHandler) {
        this.basicAuthenticationSuccessHandler = basicAuthenticationSuccessHandler;
    }
}
