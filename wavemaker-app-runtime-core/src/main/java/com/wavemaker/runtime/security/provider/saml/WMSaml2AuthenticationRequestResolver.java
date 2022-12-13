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

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2PostAuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2RedirectAuthenticationRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;

import com.wavemaker.runtime.security.provider.saml.util.SamlUtils;

public class WMSaml2AuthenticationRequestResolver implements Saml2AuthenticationRequestResolver {

    RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;
    Saml2AuthenticationRequestResolver openSamlAuthenticationRequestResolver;

    public WMSaml2AuthenticationRequestResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver, Saml2AuthenticationRequestResolver openSamlAuthenticationRequestResolver) {
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
        this.openSamlAuthenticationRequestResolver = openSamlAuthenticationRequestResolver;
    }

    @Override
    public AbstractSaml2AuthenticationRequest resolve(HttpServletRequest request) {
        RelyingPartyRegistration registration = this.relyingPartyRegistrationResolver.resolve(request, null);
        AbstractSaml2AuthenticationRequest saml2AuthenticationRequest = this.openSamlAuthenticationRequestResolver.resolve(request);
        String relayState = SamlUtils.resolveRelayState(request);
        if (saml2AuthenticationRequest.getBinding() == Saml2MessageBinding.POST) {
            Saml2PostAuthenticationRequest saml2PostAuthenticationRequest = (Saml2PostAuthenticationRequest) saml2AuthenticationRequest;
            return Saml2PostAuthenticationRequest.withRelyingPartyRegistration(registration)
                .samlRequest(saml2PostAuthenticationRequest.getSamlRequest())
                .authenticationRequestUri(saml2PostAuthenticationRequest.getAuthenticationRequestUri())
                .relayState(relayState)
                .build();
        } else {
            Saml2RedirectAuthenticationRequest saml2RedirectAuthenticationRequest = (Saml2RedirectAuthenticationRequest) saml2AuthenticationRequest;
            return Saml2RedirectAuthenticationRequest.withRelyingPartyRegistration(registration)
                .samlRequest(saml2RedirectAuthenticationRequest.getSamlRequest())
                .authenticationRequestUri(saml2RedirectAuthenticationRequest.getAuthenticationRequestUri())
                .relayState(relayState)
                .build();
        }
    }
}
