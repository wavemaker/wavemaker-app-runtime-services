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

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import com.google.common.collect.Sets;

import static com.wavemaker.runtime.security.constants.SecurityConstants.AD_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.CAS_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.CUSTOM_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.DATABASE_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.DEMO_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.JWS_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.LDAP_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.OPAQUE_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.OPENID_PROVIDER;
import static com.wavemaker.runtime.security.constants.SecurityConstants.SAML_PROVIDER;

public class SecurityPropertyUtils {

    private SecurityPropertyUtils() {
    }

    public static Set<String> getActiveProviders(Environment environment) {
        String activeProviderStr = environment.getProperty("security.activeProviders");
        if (StringUtils.isBlank(activeProviderStr)) {
            return Collections.emptySet();
        } else {
            return Sets.newHashSet(StringUtils.split(activeProviderStr, ','));
        }
    }

    public static void validateActiveProviders(Environment environment) {
        Set<String> activeProviders = getActiveProviders(environment);
        boolean validActiveProviders = !activeProviders.isEmpty() && Set.of(DEMO_PROVIDER, DATABASE_PROVIDER, LDAP_PROVIDER, AD_PROVIDER, CAS_PROVIDER, SAML_PROVIDER,
            OPENID_PROVIDER, OPAQUE_PROVIDER, JWS_PROVIDER, CUSTOM_PROVIDER).containsAll(activeProviders);
        if (!validActiveProviders) {
            throw new IllegalStateException("Invalid value for the security.activeProviders " + activeProviders);
        }
    }
}
