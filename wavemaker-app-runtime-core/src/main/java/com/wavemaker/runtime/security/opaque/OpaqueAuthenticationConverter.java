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

package com.wavemaker.runtime.security.opaque;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;

import com.wavemaker.runtime.security.WMAuthentication;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;

public class OpaqueAuthenticationConverter implements OpaqueTokenAuthenticationConverter {

    @Value("${security.providers.opaqueToken.roleMappingEnabled}")
    private boolean roleMappingEnabled;
    @Value("${security.providers.opaqueToken.roleProvider}")
    private String roleProvider;
    @Value("${security.providers.opaqueToken.roleAttributeName}")
    private String roleAttributeName;
    @Value("${security.providers.opaqueToken.principalClaimName}")
    private String principalClaimName;
    @Autowired(required = false)
    private AuthoritiesProvider authoritiesProvider;

    @Override
    public Authentication convert(String introspectedToken, OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        Instant iat = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.IAT);
        Instant exp = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.EXP);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, introspectedToken,
            iat, exp);
        Object principalAttributeValue = authenticatedPrincipal.getAttribute(principalClaimName);
        BearerTokenAuthentication bearerTokenAuthentication;
        OAuth2AuthenticatedPrincipal updatedOAuth2AuthenticatedPrincipal;
        if (!roleMappingEnabled) {
            updatedOAuth2AuthenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(principalAttributeValue.toString(),
                authenticatedPrincipal.getAttributes(), (Collection<GrantedAuthority>) authenticatedPrincipal.getAuthorities());
            bearerTokenAuthentication = new BearerTokenAuthentication(updatedOAuth2AuthenticatedPrincipal, accessToken,
                updatedOAuth2AuthenticatedPrincipal.getAuthorities());
        } else {
            List<GrantedAuthority> authorities;
            if (authoritiesProvider != null) {
                authorities = authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(authenticatedPrincipal.getName()));
            } else {
                OpaqueRoleAttributeAuthoritiesExtractor opaqueRoleAttributeAuthoritiesExtractor = new OpaqueRoleAttributeAuthoritiesExtractor();
                authorities = opaqueRoleAttributeAuthoritiesExtractor.getAuthorities(authenticatedPrincipal, roleAttributeName);
            }
            authorities.addAll(authenticatedPrincipal.getAuthorities());
            updatedOAuth2AuthenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(principalAttributeValue.toString(),
                authenticatedPrincipal.getAttributes(), authorities);
            bearerTokenAuthentication = new BearerTokenAuthentication(updatedOAuth2AuthenticatedPrincipal, accessToken,
                updatedOAuth2AuthenticatedPrincipal.getAuthorities());
        }
        return new WMAuthentication(bearerTokenAuthentication);
    }

    public void setAuthoritiesProvider(AuthoritiesProvider authoritiesProvider) {
        this.authoritiesProvider = authoritiesProvider;
    }
}
