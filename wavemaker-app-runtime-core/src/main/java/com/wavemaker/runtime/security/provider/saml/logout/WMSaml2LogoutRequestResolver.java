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

package com.wavemaker.runtime.security.provider.saml.logout;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.provider.saml.util.SamlUtils;

public class WMSaml2LogoutRequestResolver implements Saml2LogoutRequestResolver {

    RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;
    Saml2LogoutRequestResolver openSamlLogoutRequestResolver;

    public WMSaml2LogoutRequestResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver,
                                        Saml2LogoutRequestResolver openSamlLogoutRequestResolver) {
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
        this.openSamlLogoutRequestResolver = openSamlLogoutRequestResolver;
    }

    @Override
    public Saml2LogoutRequest resolve(HttpServletRequest request, Authentication authentication) {
        Saml2LogoutRequest saml2LogoutRequest = openSamlLogoutRequestResolver.resolve(request, authentication);
        if (saml2LogoutRequest == null) {
            throw new WMRuntimeException("Error creating saml2 logout request. Please check if the idp metadata contains logoutService definition");
        }
        RelyingPartyRegistration relyingParty = this.relyingPartyRegistrationResolver.resolve(request, "saml");
        return Saml2LogoutRequest.withRelyingPartyRegistration(relyingParty)
            .relayState(SamlUtils.resolveRelayState(request))
            .samlRequest(saml2LogoutRequest.getSamlRequest())
            .id(saml2LogoutRequest.getId())
            .binding(saml2LogoutRequest.getBinding()).build();
    }
}
