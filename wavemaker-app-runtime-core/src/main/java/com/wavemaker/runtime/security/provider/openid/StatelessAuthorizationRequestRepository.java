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

package com.wavemaker.runtime.security.provider.openid;

import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.security.provider.openid.util.OpenIdUtils;

public class StatelessAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(StatelessAuthorizationRequestRepository.class);

    HttpSessionOAuth2AuthorizationRequestRepository delegate = new HttpSessionOAuth2AuthorizationRequestRepository();

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = this.delegate.loadAuthorizationRequest(request);
        if (oAuth2AuthorizationRequest != null) {
            logger.info("Using the existing AuthorizationRequest");
            return oAuth2AuthorizationRequest;
        }
        logger.info("Creating a new AuthorizationRequest");
        String[] split = request.getServletPath().split("/");
        String clientId = split[split.length - 1];
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientId);
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode();
        String state = request.getParameter("state") != null ? request.getParameter("state") : "e30=";
        String redirectUri = request.getParameter("redirect_uri") != null ? request.getParameter("redirect_uri") :
            OpenIdUtils.getRedirectUri(clientId, HttpRequestUtils.getApplicationBaseUrl(request));
        return builder
            .clientId(clientId)
            .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
            .scopes(clientRegistration.getScopes())
            .additionalParameters(Collections.singletonMap("registration_id", clientId))
            .state(state)
            .redirectUri(redirectUri).build();
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        this.delegate.saveAuthorizationRequest(authorizationRequest, request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.delegate.removeAuthorizationRequest(request, response);
    }
}

