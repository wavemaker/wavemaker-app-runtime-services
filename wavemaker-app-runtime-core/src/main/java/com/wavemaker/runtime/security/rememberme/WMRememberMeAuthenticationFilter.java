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
package com.wavemaker.runtime.security.rememberme;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Kishore Routhu on 6/2/18 4:47 PM.
 */
public class WMRememberMeAuthenticationFilter extends GenericFilterBean implements
    ApplicationEventPublisherAware {

    // ~ Instance fields
    // ================================================================================================

    private ApplicationEventPublisher eventPublisher;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationManager authenticationManager;
    private RememberMeServices rememberMeServices;
    private SecurityContextRepository securityContextRepository;

    public WMRememberMeAuthenticationFilter(
        AuthenticationManager authenticationManager,
        RememberMeServices rememberMeServices) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.notNull(rememberMeServices, "rememberMeServices cannot be null");
        this.authenticationManager = authenticationManager;
        this.rememberMeServices = rememberMeServices;
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationManager, "authenticationManager must be specified");
        Assert.notNull(rememberMeServices, "rememberMeServices must be specified");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication rememberMeAuth = rememberMeServices.autoLogin(request,
                response);

            boolean isRememberMeAuthentication = false;
            if (rememberMeAuth != null) {
                // Attempt authenticaton via AuthenticationManager
                try {
                    rememberMeAuth = authenticationManager.authenticate(rememberMeAuth);

                    // Store to SecurityContextHolder and SecurityContextRepository
                    SecurityContext context = SecurityContextHolder.getContext();
                    context.setAuthentication(rememberMeAuth);
                    securityContextRepository.saveContext(context, request, response);
                    isRememberMeAuthentication = true;

                    if (logger.isDebugEnabled()) {
                        logger.debug("SecurityContextHolder populated with remember-me token: '"
                            + SecurityContextHolder.getContext().getAuthentication()
                            + "'");
                    }

                    // Fire event
                    if (this.eventPublisher != null) {
                        eventPublisher
                            .publishEvent(new InteractiveAuthenticationSuccessEvent(
                                SecurityContextHolder.getContext()
                                    .getAuthentication(), this.getClass()));
                    }

                    if (successHandler != null) {
                        successHandler.onAuthenticationSuccess(request, response,
                            rememberMeAuth);
                    }

                } catch (AuthenticationException authenticationException) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "SecurityContextHolder not populated with remember-me token, as "
                                + "AuthenticationManager rejected Authentication returned by RememberMeServices: '"
                                + rememberMeAuth
                                + "'; invalidating remember-me token",
                            authenticationException);
                    }

                    rememberMeServices.loginFail(request, response);
                    onUnsuccessfulAuthentication(request, response,
                        authenticationException);
                }
            }
            chain.doFilter(request, response);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("SecurityContextHolder not populated with remember-me token, as it already contained: '"
                    + SecurityContextHolder.getContext().getAuthentication() + "'");
            }

            chain.doFilter(request, response);
        }
    }

    /**
     * Called if the {@code AuthenticationManager} rejects the authentication object
     * returned from the {@code RememberMeServices} {@code autoLogin} method. This method
     * will not be called when no remember-me token is present in the request and
     * {@code autoLogin} reurns null.
     */
    protected void onUnsuccessfulAuthentication(
        HttpServletRequest request,
        HttpServletResponse response, AuthenticationException failed) {
    }

    public RememberMeServices getRememberMeServices() {
        return rememberMeServices;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Allows control over the destination a remembered user is sent to when they are
     * successfully authenticated. By default, the filter will just allow the current
     * request to proceed, but if an {@code AuthenticationSuccessHandler} is set, it will
     * be invoked and the {@code doFilter()} method will return immediately, thus allowing
     * the application to redirect the user to a specific URL, regardless of whatthe
     * original request was for.
     *
     * @param successHandler the strategy to invoke immediately before returning from
     *                       {@code doFilter()}.
     */
    public void setAuthenticationSuccessHandler(
        AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }
}