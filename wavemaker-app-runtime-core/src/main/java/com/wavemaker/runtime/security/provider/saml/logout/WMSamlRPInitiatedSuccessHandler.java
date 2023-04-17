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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.wavemaker.runtime.security.WMAuthentication;

public class WMSamlRPInitiatedSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(WMSamlRPInitiatedSuccessHandler.class);

    private final Saml2RelyingPartyInitiatedLogoutSuccessHandler relyingPartyInitiatedLogoutSuccessHandler;

    public WMSamlRPInitiatedSuccessHandler(Saml2LogoutRequestResolver saml2LogoutRequestResolver) {
        relyingPartyInitiatedLogoutSuccessHandler = new Saml2RelyingPartyInitiatedLogoutSuccessHandler(saml2LogoutRequestResolver);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.debug("Successfully logged out from the application... creating saml sso logout request");
        Authentication authentication1;
        if (authentication instanceof WMAuthentication) {
            WMAuthentication wmAuthentication = (WMAuthentication) authentication;
            authentication1 = wmAuthentication.getAuthenticationSource();
        } else {
            authentication1 = authentication;
        }
        relyingPartyInitiatedLogoutSuccessHandler.onLogoutSuccess(request, response, authentication1);
    }
}
