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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Sets;
import com.wavemaker.runtime.security.constants.SecurityConstants;
import com.wavemaker.runtime.security.constants.SecurityProviders;

public class SecurityPropertyUtils {

    private SecurityPropertyUtils() {
    }

    public static Set<String> getActiveProviderTypes(Environment environment) {
        String activeProviderStr = environment.getProperty("security.activeProviders");
        if (StringUtils.isBlank(activeProviderStr)) {
            return Collections.emptySet();
        } else {
            Set<String> activeProviders = Sets.newHashSet(StringUtils.split(activeProviderStr, ','));
            return activeProviders.stream().map(str -> str.contains(SecurityConstants.PROVIDER_ID_SEPARATOR) ?
                str.substring(0, str.indexOf(SecurityConstants.PROVIDER_ID_SEPARATOR)) : str).collect(Collectors.toSet());
        }
    }

    public static void validateActiveProviders(Environment environment) {
        Set<String> activeProviderTypes = getActiveProviderTypes(environment);
        boolean validActiveProviders = !activeProviderTypes.isEmpty() && SecurityProviders.getProviders().containsAll(activeProviderTypes);
        if (!validActiveProviders) {
            throw new IllegalStateException("Invalid value for the security.activeProviders " + activeProviderTypes);
        }
    }

    public static List<String> getProviderIds(Environment environment, String providerType) {
        String activeProviderStr = environment.getProperty("security.activeProviders");
        if (StringUtils.isBlank(activeProviderStr)) {
            return Collections.emptyList();
        } else {
            List<String> providerIds = new ArrayList<>();
            List<String> activeProviders = List.of(StringUtils.split(activeProviderStr, ','));
            activeProviders.forEach(provider -> {
                if (provider.startsWith(providerType)) {
                    provider = provider.substring(provider.indexOf(SecurityConstants.PROVIDER_ID_SEPARATOR) + 1);
                    providerIds.add(provider);
                }
            });
            return providerIds;
        }
    }

    public static MultiValueMap<String, String> getProviderIdVsProviderType(Environment environment) {
        String activeProviderStr = environment.getProperty("security.activeProviders");
        if (StringUtils.isBlank(activeProviderStr)) {
            return new LinkedMultiValueMap<>();
        } else {
            MultiValueMap<String, String> providerTypeVsProviderId = new LinkedMultiValueMap<>();
            List<String> activeProviders = Arrays.stream(StringUtils.split(activeProviderStr, ',')).toList();
            for (String s : activeProviders) {
                if (s.contains(SecurityConstants.PROVIDER_ID_SEPARATOR)) {
                    String[] providerTypeAndProviderId = s.split(SecurityConstants.PROVIDER_ID_SEPARATOR);
                    if (providerTypeVsProviderId.containsKey(providerTypeAndProviderId[0])) {
                        providerTypeVsProviderId.add(providerTypeAndProviderId[0], providerTypeAndProviderId[1]);
                    } else {
                        List<String> values = new ArrayList<>();
                        values.add(providerTypeAndProviderId[1]);
                        providerTypeVsProviderId.put(providerTypeAndProviderId[0], values);
                    }
                } else {
                    providerTypeVsProviderId.put(s, List.of(s));
                }
            }
            return providerTypeVsProviderId;
        }
    }
}
