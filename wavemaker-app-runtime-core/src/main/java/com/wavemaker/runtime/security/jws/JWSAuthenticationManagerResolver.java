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

package com.wavemaker.runtime.security.jws;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;

public class JWSAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

    @Autowired
    private Environment environment;
    @Autowired
    private ApplicationContext applicationContext;
    @Value("${security.providers.jws}")
    private String jwsProviders;

    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    private Predicate<String> trustedIssuer;

    private Map<String, String> issuerUrlVsProviderId = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        List<String> jwsProvidersList = Arrays.stream(jwsProviders.split(",")).collect(Collectors.toList());
        jwsProvidersList.forEach(jwsProviderId -> {
            String issuerUrl = environment.getProperty("security.providers.jws." + jwsProviderId + ".issuerUrl");
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
                    String principalClaimName = environment.getProperty("security.providers.jws." + jwsProviderId + ".principalClaimName"
                        , "sub");
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(getJwtGrantedAuthoritiesConverter(jwsProviderId));
                    jwtAuthenticationConverter.setPrincipalClaimName(principalClaimName);
                    JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
                    jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
                    return new JWSAuthenticationProvider(jwtAuthenticationProvider)::authenticate;
                });
        }
        return null;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> getJwtGrantedAuthoritiesConverter(String jwsProviderId) {
        boolean isRoleMappingEnabled = Boolean.parseBoolean(environment.getProperty("security.providers.jws." + jwsProviderId + ".roleMappingEnabled"
            , "false"));
        if (isRoleMappingEnabled) {
            String jwsRoleProvider = environment.getProperty("security.providers.jws." + jwsProviderId + ".roleProvider");
            if (Objects.equals(jwsRoleProvider, "JWS")) {
                String jwsRoleAttributeName = environment.getProperty("security.providers.jws." + jwsProviderId + ".roleAttributeName");
                JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
                jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(jwsRoleAttributeName);
                return jwtGrantedAuthoritiesConverter;
            } else if (Objects.equals(jwsRoleProvider, "Database")) {
                String principalClaimName = environment.getProperty("security.providers.jws." + jwsProviderId + ".principalClaimName");
                AuthoritiesProvider authoritiesProvider = (AuthoritiesProvider) applicationContext.getBean(jwsProviderId + "JWSAuthoritiesProvider");
                return new JWSDatabaseGrantedAuthoritiesConverter(principalClaimName, authoritiesProvider);
            } else {
                return new JwtGrantedAuthoritiesConverter();
            }
        } else {
            return new JwtGrantedAuthoritiesConverter();
        }
    }
}
