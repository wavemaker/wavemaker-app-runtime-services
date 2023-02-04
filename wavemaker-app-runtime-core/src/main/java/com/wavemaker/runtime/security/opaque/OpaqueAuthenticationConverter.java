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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Value("${security.providers.opaqueToken.roleMappingEnabled:false}")
    private boolean roleMappingEnabled;
    @Value("${security.providers.opaqueToken.roleProvider:#{null}}")
    private String roleProvider;
    @Value("${security.providers.opaqueToken.roleAttributeName:#{null}}")
    private String roleAttributeName;
    @Value("${security.providers.opaqueToken.principalClaimName:sub}")
    private String principalClaimName;
    @Autowired(required = false)
    @Qualifier("opaqueAuthoritiesProvider")
    private AuthoritiesProvider authoritiesProvider;

    @Override
    public Authentication convert(String introspectedToken, OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        Instant iat = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.IAT);
        Instant exp = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.EXP);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, introspectedToken,
            iat, exp);
        String principalAttributeValue = authenticatedPrincipal.getAttribute(principalClaimName);
        Collection<GrantedAuthority> grantedAuthorities;
        if (roleMappingEnabled) {
            grantedAuthorities = resolveGrantedAuthorities(authenticatedPrincipal);
        } else {
            grantedAuthorities = (Collection<GrantedAuthority>) authenticatedPrincipal.getAuthorities();
        }
        OAuth2AuthenticatedPrincipal grantedAuthoritiesOAuth2AuthenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(principalAttributeValue,
            authenticatedPrincipal.getAttributes(), grantedAuthorities);
        BearerTokenAuthentication bearerTokenAuthentication = new BearerTokenAuthentication(grantedAuthoritiesOAuth2AuthenticatedPrincipal, accessToken,
            grantedAuthoritiesOAuth2AuthenticatedPrincipal.getAuthorities());
        return new WMAuthentication(bearerTokenAuthentication);
    }

    private List<GrantedAuthority> resolveGrantedAuthorities(OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (roleMappingEnabled && StringUtils.isNotBlank(roleProvider)) {
            if (authoritiesProvider != null) {
                // roles are from database
                grantedAuthorities.addAll(authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(authenticatedPrincipal.getName())));
            } else {
                // roles are from role attribute
                List<GrantedAuthority> authorities = getAuthoritiesFromOAuth2AuthenticatedPrincipal(authenticatedPrincipal, roleAttributeName);
                grantedAuthorities.addAll(authorities);
            }
        }
        return grantedAuthorities;
    }

    private List<GrantedAuthority> getAuthoritiesFromOAuth2AuthenticatedPrincipal(OAuth2AuthenticatedPrincipal authenticatedPrincipal, String roleAttributeName) {
        Object authorities = authenticatedPrincipal.getAttribute(roleAttributeName);
        return getAuthorities(authorities).stream().
            map(authority -> new SimpleGrantedAuthority("ROLE_" + authority)).collect(Collectors.toList());
    }

    private Collection<String> getAuthorities(Object authorities) {
        if (authorities instanceof String) {
            if (org.springframework.util.StringUtils.hasText((String) authorities)) {
                return Arrays.asList(((String) authorities).split(" "));
            }
            return Collections.emptyList();
        }
        if (authorities instanceof Collection) {
            return (Collection<String>) authorities;
        }
        return Collections.emptyList();
    }
}
