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
package com.wavemaker.runtime.security.entrypoint;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.wavemaker.runtime.security.WMAuthenticationEntryPoint;
import com.wavemaker.runtime.security.model.AuthProvider;
import com.wavemaker.runtime.security.utils.SecurityPropertyUtils;

/**
 * Created by srujant on 2/8/18.
 */
public class WMCompositeAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(WMCompositeAuthenticationEntryPoint.class);

    @Autowired
    private AuthenticationEntryPointRegistry authenticationEntryPointRegistry;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        AuthenticationEntryPoint authenticationEntryPoint = authenticationEntryPointRegistry.getUniqueAuthenticationEntryPoint();
        if (authenticationEntryPoint == null) {
            String providerId = request.getParameter("providerId");
            if (providerId != null) {
                AuthProvider authProvider = SecurityPropertyUtils.getAuthProvider(providerId);
                logger.info("Using authentication entry point for providerId {}", providerId);
                authenticationEntryPoint = authenticationEntryPointRegistry.getAuthenticationEntryPoint(authProvider);
            }
        }
        if (authenticationEntryPoint == null) {
            logger.info("As multiple AuthenticationEntryPoints is configured, commencing the request to index.html");
            authenticationEntryPoint = new WMAuthenticationEntryPoint("/index.html");
        }
        authenticationEntryPoint.commence(request, response, authException);
    }
}
