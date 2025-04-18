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
package com.wavemaker.runtime.security.provider.openid;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import com.wavemaker.commons.auth.oauth2.OAuth2Helper;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.security.provider.openid.util.OpenIdUtils;

/**
 * Filter class to redirect the request to the OpenId authentication provider configured in the application.
 *
 * Created by srujant on 6/8/18.
 */
public class OpenIDAuthorizationRequestRedirectFilter extends OncePerRequestFilter {

    public static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";
    private final AntPathRequestMatcher authorizationRequestMatcher;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RedirectStrategy authorizationRedirectStrategy = new DefaultRedirectStrategy();
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
        new HttpSessionOAuth2AuthorizationRequestRepository();

    /**
     * Constructs an {@code OAuth2AuthorizationRequestRedirectFilter} using the provided parameters.
     *
     * @param clientRegistrationRepository the repository of client registrations
     */
    public OpenIDAuthorizationRequestRedirectFilter(ClientRegistrationRepository clientRegistrationRepository) {
        this(clientRegistrationRepository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    }

    /**
     * Constructs an {@code OAuth2AuthorizationRequestRedirectFilter} using the provided parameters.
     *
     * @param clientRegistrationRepository the repository of client registrations
     * @param authorizationRequestBaseUri  the base {@code URI} used for authorization requests
     */
    public OpenIDAuthorizationRequestRedirectFilter(
        ClientRegistrationRepository clientRegistrationRepository, String authorizationRequestBaseUri) {

        Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        this.authorizationRequestMatcher = new AntPathRequestMatcher(
            authorizationRequestBaseUri + "/{" + OpenIdConstants.REGISTRATION_ID_URI_VARIABLE_NAME + "}");
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /**
     * Sets the repository used for storing {@link OAuth2AuthorizationRequest}'s.
     *
     * @param authorizationRequestRepository the repository used for storing {@link OAuth2AuthorizationRequest}'s
     */
    public final void setAuthorizationRequestRepository(AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        if (this.shouldRequestAuthorization(request)) {
            try {
                this.sendRedirectForAuthorization(request, response);
            } catch (Exception failed) {
                this.unsuccessfulRedirectForAuthorization(request, response, failed);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldRequestAuthorization(HttpServletRequest request) {
        return this.authorizationRequestMatcher.matches(request);
    }

    private void sendRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        String registrationId = this.authorizationRequestMatcher
            .extractUriTemplateVariables(request).get(OpenIdConstants.REGISTRATION_ID_URI_VARIABLE_NAME);
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Invalid Client Registration with Id: " + registrationId);
        }

        OAuth2AuthorizationRequest.Builder builder;
        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(clientRegistration.getAuthorizationGrantType())) {
            builder = OAuth2AuthorizationRequest.authorizationCode();
        } else {
            throw new IllegalArgumentException("Invalid Authorization Grant Type for Client Registration (" +
                clientRegistration.getRegistrationId() + "): " + clientRegistration.getAuthorizationGrantType());
        }

        String appPath = HttpRequestUtils.getApplicationBaseUrl(request);
        String redirectUrl = OpenIdUtils.getRedirectUri(clientRegistration.getRegistrationId(), appPath);

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put(OpenIdConstants.REGISTRATION_ID, clientRegistration.getRegistrationId());

        Map<String, String> stateObject = new HashMap<>();
        if (RuntimeEnvironment.isTestRunEnvironment()) {
            stateObject.put(OpenIdConstants.APP_PATH, appPath);
            stateObject.put(OpenIdConstants.REGISTRATION_ID_URI_VARIABLE_NAME, clientRegistration.getRegistrationId());
            stateObject.put(OpenIdConstants.REDIRECT_URI, redirectUrl);
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(request.getParameter(OpenIdConstants.REDIRECT_PAGE))) {
            stateObject.put(OpenIdConstants.REDIRECT_PAGE, request.getParameter(OpenIdConstants.REDIRECT_PAGE));
        }
        String encodedState = OAuth2Helper.getStateParameterValue(stateObject);

        OAuth2AuthorizationRequest authorizationRequest = builder
            .clientId(clientRegistration.getClientId())
            .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
            .redirectUri(redirectUrl)
            .scopes(clientRegistration.getScopes())
            .state(encodedState)
            .additionalParameters(additionalParameters)
            .build();
        URI redirectUri = this.buildURI(authorizationRequest);

        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationRequest.getGrantType())) {
            this.authorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        }

        this.authorizationRedirectStrategy.sendRedirect(request, response, redirectUri.toString());
    }

    private URI buildURI(OAuth2AuthorizationRequest authorizationRequest) {
        Assert.notNull(authorizationRequest, "authorizationRequest cannot be null");
        Set<String> scopes = authorizationRequest.getScopes();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromUriString(authorizationRequest.getAuthorizationUri())
            .queryParam(OpenIdConstants.RESPONSE_TYPE, authorizationRequest.getResponseType().getValue())
            .queryParam(OpenIdConstants.CLIENT_ID, authorizationRequest.getClientId())
            .queryParam(OpenIdConstants.SCOPE, StringUtils.collectionToDelimitedString(scopes, " "))
            .queryParam(OpenIdConstants.STATE, authorizationRequest.getState());
        if (authorizationRequest.getRedirectUri() != null) {
            uriBuilder.queryParam(OpenIdConstants.REDIRECT_URI, authorizationRequest.getRedirectUri());
        }
        return uriBuilder.build().encode().toUri();
    }

    private void unsuccessfulRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response,
                                                      Exception failed) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Authorization Request failed: " + failed, failed);
        }
        response.sendError(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

}
