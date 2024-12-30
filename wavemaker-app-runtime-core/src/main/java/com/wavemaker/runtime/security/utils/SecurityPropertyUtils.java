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

package com.wavemaker.runtime.security.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.model.AuthProvider;
import com.wavemaker.runtime.security.model.AuthProviderType;

public class SecurityPropertyUtils {

    private static final String PROVIDER_ID_SEPARATOR = ".";

    private SecurityPropertyUtils() {
    }

    public static Set<AuthProviderType> getActiveAuthProviderTypes(Environment environment) {
        Set<AuthProviderType> authProviderTypeSet = new LinkedHashSet<>();
        Set<AuthProvider> authProviders = getAuthProviders(environment);
        for (AuthProvider authProvider : authProviders) {
            authProviderTypeSet.add(authProvider.getAuthProviderType());
        }
        return authProviderTypeSet;
    }

    public static Set<String> getProviderIds(Environment environment, AuthProviderType authProviderType) {
        Set<AuthProvider> authProviders = getAuthProviders(environment);
        Set<String> providerIds = new LinkedHashSet<>();
        for (AuthProvider authProvider : authProviders) {
            if (authProvider.getAuthProviderType() == authProviderType) {
                providerIds.add(authProvider.getProviderId());
            }
        }
        return providerIds;
    }

    public static Set<AuthProvider> getAuthProviders(Environment environment) {
        String activeProviderStr = environment.getProperty("security.activeProviders");
        if (StringUtils.isBlank(activeProviderStr)) {
            return new LinkedHashSet<>();
        } else {
            Set<AuthProvider> authProviderSet = new LinkedHashSet<>();
            String[] activeProviders = activeProviderStr.split(",");
            for (String activeProvider : activeProviders) {
                AuthProviderType authProviderType;
                String providerId = null;
                if (activeProvider.contains(PROVIDER_ID_SEPARATOR)) {
                    String[] split = StringUtils.split(activeProvider, PROVIDER_ID_SEPARATOR);
                    if (split.length != 2) {
                        throw new WMRuntimeException("Invalid security provider " + activeProvider);
                    }
                    authProviderType = AuthProviderType.valueOf(split[0]);
                    providerId = split[1];
                } else {
                    authProviderType = AuthProviderType.valueOf(activeProvider);
                }
                if (authProviderType.isMultiInstance() && StringUtils.isBlank(providerId)) {
                    throw new WMRuntimeException("providerId is missing for " + authProviderType);
                }
                authProviderSet.add(new AuthProvider(authProviderType, providerId));
            }
            return authProviderSet;
        }
    }
}
