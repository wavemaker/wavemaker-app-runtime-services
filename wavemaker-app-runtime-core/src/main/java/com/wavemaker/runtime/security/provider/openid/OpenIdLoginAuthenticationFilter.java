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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This class get's the id_token from the Identity provider using the code sent by the authorization server and returns
 * {@link WMAuthenticationToken} on successful authentication by the Identity provider.
 * Created by srujant on 6/8/18.
 */
public class OpenIdLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    /**
     * The default {@code URI} where this {@code Filter} processes authentication requests.
     */
    public static final String DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/code/*";
    private static final String AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE = "authorization_request_not_found";
    private static final String CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE = "client_registration_not_found";
    private static final Logger logger = LoggerFactory.getLogger(OpenIdLoginAuthenticationFilter.class);
    private ClientRegistrationRepository clientRegistrationRepository;
    private OAuth2AuthorizedClientService authorizedClientService;
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
        new HttpSessionOAuth2AuthorizationRequestRepository();

    /**
     * Constructs an {@code OAuth2LoginAuthenticationFilter} using the provided parameters.
     *
     * @param clientRegistrationRepository the repository of client registrations
     * @param authorizedClientService      the authorized client service
     */
    public OpenIdLoginAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository,
                                           OAuth2AuthorizedClientService authorizedClientService) {
        this(clientRegistrationRepository, authorizedClientService, DEFAULT_FILTER_PROCESSES_URI);
    }

    /**
     * Constructs an {@code OAuth2LoginAuthenticationFilter} using the provided parameters.
     *
     * @param clientRegistrationRepository the repository of client registrations
     * @param authorizedClientService      the authorized client service
     * @param filterProcessesUrl           the {@code URI} where this {@code Filter} will process the authentication requests
     */
    public OpenIdLoginAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository,
                                           OAuth2AuthorizedClientService authorizedClientService,
                                           String filterProcessesUrl) {
        super(filterProcessesUrl);
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(authorizedClientService, "authorizedClientService cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {

        if (!this.authorizationResponseSuccess(request) && !this.authorizationResponseError(request)) {
            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository.loadAuthorizationRequest(request);

        if (authorizationRequest == null) {
            OAuth2Error oauth2Error = new OAuth2Error(AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        String redirectURI = request.getParameter(OpenIdConstants.REDIRECT_URI);
        if (StringUtils.isEmpty(redirectURI)) {
            redirectURI = authorizationRequest.getRedirectUri();
        }
        request.setAttribute(OpenIdConstants.REDIRECT_URI, redirectURI);

        this.authorizationRequestRepository.removeAuthorizationRequest(request, response);

        String registrationId = (String) authorizationRequest.getAdditionalParameters().get(OpenIdConstants.REGISTRATION_ID);
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            OAuth2Error oauth2Error = new OAuth2Error(CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE,
                "Client Registration not found with Id: " + registrationId, null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        OAuth2AuthorizationResponse authorizationResponse = this.convert(request);

        OAuth2LoginAuthenticationToken authenticationRequest = new OAuth2LoginAuthenticationToken(
            clientRegistration, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
        authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

        OAuth2LoginAuthenticationToken authenticationResult = null;
        try {
            authenticationResult =
                (OAuth2LoginAuthenticationToken) this.getAuthenticationManager().authenticate(authenticationRequest);
        } catch (OAuth2AuthenticationException e) {
            logger.error("Could not authenticate using OpenID", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Could not authenticate the user : " + e.getError());
            return authenticationResult;
        } catch (Exception e) {
            logger.error("Could not authenticate using OpenID", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Could not authenticate the user");
            return authenticationResult;
        }

        OAuth2AuthenticationToken oauth2Authentication = new OAuth2AuthenticationToken(
            authenticationResult.getPrincipal(),
            authenticationResult.getAuthorities(),
            authenticationResult.getClientRegistration().getRegistrationId());

        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
            authenticationResult.getClientRegistration(),
            oauth2Authentication.getName(),
            authenticationResult.getAccessToken());

        this.authorizedClientService.saveAuthorizedClient(authorizedClient, oauth2Authentication);
        return authenticationResult;
    }

    /**
     * Sets the repository for stored {@link OAuth2AuthorizationRequest}'s.
     *
     * @param authorizationRequestRepository the repository for stored {@link OAuth2AuthorizationRequest}'s
     */
    public final void setAuthorizationRequestRepository(AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    private OAuth2AuthorizationResponse convert(HttpServletRequest request) {
        String code = request.getParameter(OpenIdConstants.CODE);
        String errorCode = request.getParameter(OpenIdConstants.ERROR);
        String state = request.getParameter(OpenIdConstants.STATE);
        String redirectUri = (String) request.getAttribute(OpenIdConstants.REDIRECT_URI);

        if (StringUtils.hasText(code)) {
            return OAuth2AuthorizationResponse.success(code)
                .redirectUri(redirectUri)
                .state(state)
                .build();
        } else {
            String errorDescription = request.getParameter(OpenIdConstants.ERROR_DESCRIPTION);
            String errorUri = request.getParameter(OpenIdConstants.ERROR_URI);
            return OAuth2AuthorizationResponse.error(errorCode)
                .redirectUri(redirectUri)
                .errorDescription(errorDescription)
                .errorUri(errorUri)
                .state(state)
                .build();
        }
    }

    private boolean authorizationResponseSuccess(HttpServletRequest request) {
        return StringUtils.hasText(request.getParameter(OpenIdConstants.CODE)) &&
            StringUtils.hasText(request.getParameter(OpenIdConstants.STATE));
    }

    private boolean authorizationResponseError(HttpServletRequest request) {
        return StringUtils.hasText(request.getParameter(OpenIdConstants.ERROR)) &&
            StringUtils.hasText(request.getParameter(OpenIdConstants.STATE));
    }
}
