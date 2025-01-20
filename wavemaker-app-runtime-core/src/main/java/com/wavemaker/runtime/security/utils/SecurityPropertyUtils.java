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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.model.AuthProvider;
import com.wavemaker.runtime.security.model.AuthProviderType;

public class SecurityPropertyUtils {

    private static final String PROVIDER_ID_SEPARATOR = ".";

    private static final Map<Environment, Set<AuthProvider>> authProvidersMap = new ConcurrentHashMap<>();

    private SecurityPropertyUtils() {
    }

    public static Set<AuthProviderType> getActiveAuthProviderTypes(Environment environment) {
        String explicitlyActivatedAuthProviderTypesStr = environment.getProperty("security.activeAuthProviderTypes");
        if (StringUtils.isNotBlank(explicitlyActivatedAuthProviderTypesStr)) {
            return Arrays.stream(explicitlyActivatedAuthProviderTypesStr.split(",")).map(AuthProviderType::valueOf).collect(Collectors.toSet());
        } else {
            Set<AuthProviderType> authProviderTypeSet = new LinkedHashSet<>();
            Set<AuthProvider> authProviders = getAllAuthProviders(environment);
            for (AuthProvider authProvider : authProviders) {
                authProviderTypeSet.add(authProvider.getAuthProviderType());
            }
            return authProviderTypeSet;
        }
    }

    public static Set<AuthProvider> getAuthProviderForType(Environment environment, AuthProviderType authProviderType) {
        Set<AuthProvider> authProviders = getActiveAuthProviders(environment);
        return authProviders.stream().filter(authProvider -> authProvider.getAuthProviderType() == authProviderType).collect(Collectors.toSet());
    }

    public static Set<AuthProvider> getActiveAuthProviders(Environment environment) {
        Set<AuthProviderType> activeAuthProviderTypes = getActiveAuthProviderTypes(environment);
        Set<AuthProvider> authProviders = getAllAuthProviders(environment);
        Set<AuthProvider> activeAuthProviders = new LinkedHashSet<>();
        for (AuthProvider authProvider : authProviders) {
            if (activeAuthProviderTypes.contains(authProvider.getAuthProviderType())) {
                activeAuthProviders.add(authProvider);
            }
        }
        return activeAuthProviders;
    }

    private static Set<AuthProvider> getAllAuthProviders(Environment environment) {
        return authProvidersMap.computeIfAbsent(environment, e -> {
            String activeProviderStr = e.getProperty("security.activeProviders");
            Set<AuthProvider> authProviderSet = new LinkedHashSet<>();
            if (StringUtils.isNotBlank(activeProviderStr)) {
                String[] activeProviders = activeProviderStr.split(",");
                for (String activeProvider : activeProviders) {
                    AuthProvider authProvider = getAuthProvider(activeProvider);
                    authProviderSet.add(authProvider);
                }
            }
            return authProviderSet;
        });
    }

    public static AuthProvider getAuthProvider(String fullProviderId) {
        AuthProviderType authProviderType;
        String providerId = null;
        if (fullProviderId.contains(PROVIDER_ID_SEPARATOR)) {
            String[] split = StringUtils.split(fullProviderId, PROVIDER_ID_SEPARATOR);
            if (split.length != 2) {
                throw new WMRuntimeException("Invalid security provider " + fullProviderId);
            }
            authProviderType = AuthProviderType.valueOf(split[0]);
            providerId = split[1];
        } else {
            authProviderType = AuthProviderType.valueOf(fullProviderId);
        }
        if (authProviderType.isMultiInstance() && StringUtils.isBlank(providerId)) {
            throw new WMRuntimeException("providerId is missing for " + authProviderType);
        }
        return new AuthProvider(authProviderType, providerId);
    }
}
