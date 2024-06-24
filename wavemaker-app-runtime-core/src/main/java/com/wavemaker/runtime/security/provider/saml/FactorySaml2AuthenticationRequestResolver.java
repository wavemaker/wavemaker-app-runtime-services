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

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestContext;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestFactory;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestContextResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

public class FactorySaml2AuthenticationRequestResolver implements Saml2AuthenticationRequestResolver {

    private final Saml2AuthenticationRequestContextResolver authenticationRequestContextResolver;

    private RequestMatcher redirectMatcher = new AntPathRequestMatcher("/saml2/authenticate/{registrationId}");

    private Saml2AuthenticationRequestFactory authenticationRequestFactory;

    FactorySaml2AuthenticationRequestResolver(
        Saml2AuthenticationRequestContextResolver authenticationRequestContextResolver,
        Saml2AuthenticationRequestFactory authenticationRequestFactory) {
        Assert.notNull(authenticationRequestContextResolver, "authenticationRequestContextResolver cannot be null");
        Assert.notNull(authenticationRequestFactory, "authenticationRequestFactory cannot be null");
        this.authenticationRequestContextResolver = authenticationRequestContextResolver;
        this.authenticationRequestFactory = authenticationRequestFactory;
    }

    @Override
    public AbstractSaml2AuthenticationRequest resolve(HttpServletRequest request) {
        RequestMatcher.MatchResult matcher = this.redirectMatcher.matcher(request);
        if (!matcher.isMatch()) {
            return null;
        }
        Saml2AuthenticationRequestContext context = this.authenticationRequestContextResolver.resolve(request);
        if (context == null) {
            return null;
        }
        Saml2MessageBinding binding = context.getRelyingPartyRegistration().getAssertingPartyDetails()
            .getSingleSignOnServiceBinding();
        if (binding == Saml2MessageBinding.REDIRECT) {
            return this.authenticationRequestFactory.createRedirectAuthenticationRequest(context);
        }
        return this.authenticationRequestFactory.createPostAuthenticationRequest(context);
    }

}