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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.wavemaker.app.security.models.config.openid.OpenIdProviderInfo;

import jakarta.annotation.PostConstruct;

/**
 * Created by srujant on 30/7/18.
 */
public class InMemoryRegistrationRepository implements ClientRegistrationRepository {
    private Map<String, ClientRegistration> registrations;

    @Autowired
    private OpenIdProviderRuntimeConfig openIdProviderRuntimeConfig;

    @PostConstruct
    public void init() {
        registrations = new HashMap<>();
        String openIdScope = "openid";
        if (openIdProviderRuntimeConfig != null && !CollectionUtils.isEmpty(openIdProviderRuntimeConfig.getOpenIdProviderInfoList())) {
            for (OpenIdProviderInfo openIdProviderInfo : openIdProviderRuntimeConfig.getOpenIdProviderInfoList()) {
                List<String> scopes = openIdProviderInfo.getScopes();
                if (!scopes.contains(openIdScope)) {
                    scopes.add(openIdScope);
                }

                ClientRegistration client = ClientRegistration.withRegistrationId(openIdProviderInfo.getProviderId())
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationUri(openIdProviderInfo.getAuthorizationUrl())
                    .tokenUri(openIdProviderInfo.getTokenUrl())
                    .jwkSetUri(openIdProviderInfo.getJwkSetUrl())
                    .userInfoUri(openIdProviderInfo.getUserInfoUrl())
                    .scope(Arrays.copyOf(scopes.toArray(), scopes.size(), String[].class))
                    .redirectUri(openIdProviderInfo.getRedirectUrlTemplate())
                    .clientId(openIdProviderInfo.getClientId())
                    .clientSecret(openIdProviderInfo.getClientSecret())
                    .clientName(openIdProviderInfo.getProviderId())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .userNameAttributeName(openIdProviderInfo.getUserNameAttributeName())
                    .build();
                registrations.put(openIdProviderInfo.getProviderId(), client);
            }
        }
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        Assert.hasText(registrationId, "registrationId cannot be empty");
        return this.registrations.get(registrationId);
    }
}
