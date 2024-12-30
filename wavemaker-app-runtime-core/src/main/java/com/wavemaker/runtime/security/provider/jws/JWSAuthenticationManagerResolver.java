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

package com.wavemaker.runtime.security.provider.jws;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.wavemaker.app.security.models.jws.JWSConfiguration;
import com.wavemaker.app.security.models.jws.JWSProviderConfiguration;
import com.wavemaker.runtime.security.authenticationprovider.WMDelegatingAuthenticationProvider;
import com.wavemaker.runtime.security.model.AuthProviderType;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.provider.authoritiesprovider.JWSAuthoritiesProviderManager;

public class JWSAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JWSConfiguration jwsConfiguration;

    @Autowired
    private JWSAuthoritiesProviderManager jwsAuthoritiesProviderManager;

    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    private Predicate<String> trustedIssuer;

    private Map<String, String> issuerUrlVsProviderId = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        jwsConfiguration.getJws().forEach((jwsProviderId, jwsProviderConfiguration) -> {
            String issuerUrl = jwsProviderConfiguration.getIssuerUrl();
            if (StringUtils.isNotBlank(issuerUrl)) {
                issuerUrlVsProviderId.put(issuerUrl, jwsProviderId);
            }
        });
        this.trustedIssuer = Collections.unmodifiableCollection(issuerUrlVsProviderId.keySet())::contains;
    }

    @Override
    public AuthenticationManager resolve(String issuer) {
        if (this.trustedIssuer.test(issuer)) {
            return this.authenticationManagers.computeIfAbsent(issuer,
                authenticationManager -> {
                    JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuer);
                    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                    String jwsProviderId = issuerUrlVsProviderId.get(issuer);
                    String principalClaimName = jwsConfiguration.getJws().get(jwsProviderId).getPrincipalClaimName();
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(getJwtGrantedAuthoritiesConverter(jwsProviderId));
                    jwtAuthenticationConverter.setPrincipalClaimName(principalClaimName);
                    JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
                    jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
                    return new WMDelegatingAuthenticationProvider(new JWSAuthenticationProvider(jwtAuthenticationProvider), AuthProviderType.JWS)::authenticate;
                });
        }
        return null;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> getJwtGrantedAuthoritiesConverter(String jwsProviderId) {
        JWSProviderConfiguration jwsProviderConfiguration = jwsConfiguration.getJws().get(jwsProviderId);
        boolean isRoleMappingEnabled = jwsProviderConfiguration.isRoleMappingEnabled();
        if (isRoleMappingEnabled) {
            String jwsRoleProvider = jwsProviderConfiguration.getRoleProvider();
            if (Objects.equals(jwsRoleProvider, "JWS")) {
                String jwsRoleAttributeName = jwsProviderConfiguration.getRoleAttributeName();
                JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
                jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(jwsRoleAttributeName);
                return jwtGrantedAuthoritiesConverter;
            } else if (Objects.equals(jwsRoleProvider, "Database")) {
                String principalClaimName = jwsProviderConfiguration.getPrincipalClaimName();
                AuthoritiesProvider authoritiesProvider = jwsAuthoritiesProviderManager.getAuthoritiesProvider(jwsProviderId);
                return new JWSDatabaseGrantedAuthoritiesConverter(principalClaimName, authoritiesProvider);
            } else {
                return new JwtGrantedAuthoritiesConverter();
            }
        } else {
            return new JwtGrantedAuthoritiesConverter();
        }
    }
}
