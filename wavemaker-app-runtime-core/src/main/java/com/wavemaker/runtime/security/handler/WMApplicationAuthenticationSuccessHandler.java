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
package com.wavemaker.runtime.security.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.WMAuthentication;

/**
 * Created by srujant on 31/10/18.
 */
public class WMApplicationAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(WMApplicationAuthenticationSuccessHandler.class);
    private List<AuthenticationSuccessHandler> defaultSuccessHandlerList = new ArrayList<>();
    @Autowired(required = false)
    private List<WMAuthenticationSuccessHandler> customSuccessHandlerList = new ArrayList<>();
    @Autowired
    private WMAuthenticationRedirectionHandler authenticationSuccessRedirectionHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        authentication = new WMAuthentication(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            invokeCustomWMAuthenticationSuccessHandler(request, response, (WMAuthentication) authentication);
            invokeDefaultAuthenticationSuccessHandlers(request, response, authentication);
            invokeRedirectionHandler(request, response, (WMAuthentication) authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new WMRuntimeException(e);
        }
    }

    private void invokeCustomWMAuthenticationSuccessHandler(HttpServletRequest request, HttpServletResponse response, WMAuthentication authentication) throws IOException, ServletException {
        if (CollectionUtils.isNotEmpty(customSuccessHandlerList)) {
            logger.info("Invoking CustomAuthenticationSuccessHandlers");
            for (WMAuthenticationSuccessHandler authenticationSuccessHandler : customSuccessHandlerList) {
                authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }

    private void invokeDefaultAuthenticationSuccessHandlers(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (CollectionUtils.isNotEmpty(defaultSuccessHandlerList)) {
            logger.info("Invoking DefaultAuthenticationSuccessHandlers");
            for (AuthenticationSuccessHandler authenticationSuccessHandler : defaultSuccessHandlerList) {
                authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }

    private void invokeRedirectionHandler(HttpServletRequest request, HttpServletResponse response, WMAuthentication authentication) throws IOException, ServletException {
        if (authenticationSuccessRedirectionHandler != null) {
            logger.info("Invoking authenticationSuccessRedirectionHandler");
            authenticationSuccessRedirectionHandler.onAuthenticationSuccess(request, response, authentication);
        }
    }

    public void setDefaultSuccessHandlerList(List<AuthenticationSuccessHandler> defaultSuccessHandlerList) {
        this.defaultSuccessHandlerList = defaultSuccessHandlerList;
    }

    public void setAuthenticationSuccessRedirectionHandler(WMAuthenticationRedirectionHandler authenticationSuccessRedirectionHandler) {
        this.authenticationSuccessRedirectionHandler = authenticationSuccessRedirectionHandler;
    }
}
