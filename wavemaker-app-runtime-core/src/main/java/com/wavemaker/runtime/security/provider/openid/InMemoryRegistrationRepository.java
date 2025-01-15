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
package com.wavemaker.runtime.security.provider.openid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;

import com.wavemaker.app.security.models.config.openid.OpenIdProviderConfig;

/**
 * Created by srujant on 30/7/18.
 */
public class InMemoryRegistrationRepository implements ClientRegistrationRepository {

    private final Map<String, ClientRegistration> registrations = new ConcurrentHashMap<>();

    @Autowired
    private OpenIdProviderRuntimeRegistry openIdProviderRuntimeRegistry;

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        Assert.hasText(registrationId, "registrationId cannot be empty");
        return registrations.computeIfAbsent(registrationId, this::buildClientRegistration);
    }

    private ClientRegistration buildClientRegistration(String registrationId) {
        OpenIdProviderConfig openIdProviderConfig = openIdProviderRuntimeRegistry.getOpenIdProviderConfig(registrationId);
        List<String> scopes = openIdProviderConfig.getScopes();

        return ClientRegistration.withRegistrationId(openIdProviderConfig.getProviderId())
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationUri(openIdProviderConfig.getAuthorizationUrl())
            .tokenUri(openIdProviderConfig.getTokenUrl())
            .jwkSetUri(openIdProviderConfig.getJwkSetUrl())
            .userInfoUri(openIdProviderConfig.getUserInfoUrl())
            .scope(Arrays.copyOf(scopes.toArray(), scopes.size(), String[].class))
            .redirectUri(openIdProviderConfig.getRedirectUrlTemplate())
            .clientId(openIdProviderConfig.getClientId())
            .clientSecret(openIdProviderConfig.getClientSecret())
            .clientName(openIdProviderConfig.getProviderId())
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .userNameAttributeName(openIdProviderConfig.getUserNameAttributeName())
            .build();
    }
}
