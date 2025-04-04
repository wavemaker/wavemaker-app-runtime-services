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
package com.wavemaker.runtime.security.provider.openid.handler;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.wavemaker.runtime.security.Attribute;
import com.wavemaker.runtime.security.WMAuthentication;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.model.AuthProviderType;
import com.wavemaker.runtime.security.provider.openid.OpenIdConstants;

/**
 * Created by srujant on 13/11/18.
 */
public class WMOpenIdAuthenticationSuccessHandler implements WMAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, WMAuthentication authentication) throws IOException, ServletException {
        if (authentication.getAuthProviderType() == AuthProviderType.OPENID) {
            OAuth2LoginAuthenticationToken oAuth2LoginAuthenticationToken = (OAuth2LoginAuthenticationToken) authentication.getAuthenticationSource();
            OidcUser oidcUser = (OidcUser) oAuth2LoginAuthenticationToken.getPrincipal();
            oidcUser.getClaims().entrySet().stream().forEach(entry -> {
                authentication.addAttribute(entry.getKey(), entry.getValue(), Attribute.AttributeScope.ALL);
            });
            String[] authenticationRequestSplit = request.getServletPath().split("/");
            String openIdProvider = authenticationRequestSplit[authenticationRequestSplit.length - 1];
            authentication.addAttribute(OpenIdConstants.PROVIDER_ID, openIdProvider, Attribute.AttributeScope.SERVER_ONLY);
            authentication.addAttribute(OpenIdConstants.ID_TOKEN_VALUE, oidcUser.getIdToken().getTokenValue(), Attribute.AttributeScope.SERVER_ONLY);
            OAuth2AccessToken accessToken = oAuth2LoginAuthenticationToken.getAccessToken();
            if (accessToken != null) {
                authentication.addAttribute(OpenIdConstants.ACCESS_TOKEN_VALUE, accessToken.getTokenValue(), Attribute.AttributeScope.SERVER_ONLY);
            }
        }
    }
}
