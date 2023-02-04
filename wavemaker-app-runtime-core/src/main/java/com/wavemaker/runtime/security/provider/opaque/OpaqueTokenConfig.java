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

package com.wavemaker.runtime.security.provider.opaque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

import com.wavemaker.runtime.security.opaque.OpaqueAuthenticationConverter;

@Configuration
public class OpaqueTokenConfig {

    @Value("${security.providers.opaqueToken.introspectionUrl}")
    private String introspectionUrl;
    @Value("${security.providers.opaqueToken.clientId}")
    private String clientId;
    @Value("${security.providers.opaqueToken.clientSecret}")
    private String clientSecret;
    @Value("${security.providers.opaqueToken.principalClaimName}")
    private String principalClaimName;

    @Bean(name = "bearerTokenAuthenticationFilter")
    public BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter() {
        return new BearerTokenAuthenticationFilter(providerManager());
    }

    @Bean(name = "providerManager")
    public ProviderManager providerManager() {
        return new ProviderManager(opaqueTokenAuthenticationProvider());
    }

    @Bean(name = "opaqueTokenAuthenticationProvider")
    public OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider() {
        OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider = new OpaqueTokenAuthenticationProvider(nimbusOpaqueTokenIntrospector());
        opaqueTokenAuthenticationProvider.setAuthenticationConverter(opaqueAuthenticationConverter());
        return opaqueTokenAuthenticationProvider;
    }

    @Bean(name = "opaqueAuthenticationConverter")
    public OpaqueAuthenticationConverter opaqueAuthenticationConverter() {
        return new OpaqueAuthenticationConverter();
    }

    @Bean(name = "nimbusOpaqueTokenIntrospector")
    public OpaqueTokenIntrospector nimbusOpaqueTokenIntrospector() {
        return new NimbusOpaqueTokenIntrospector(introspectionUrl, clientId, clientSecret);
    }
}
