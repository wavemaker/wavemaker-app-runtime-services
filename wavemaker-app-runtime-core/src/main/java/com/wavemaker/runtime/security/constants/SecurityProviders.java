/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.security.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public enum SecurityProviders {

    DEMO("DEMO", AuthenticationMode.USERNAME_PASSWORD, true, false),
    DATABASE("DATABASE", AuthenticationMode.USERNAME_PASSWORD, true, false),
    LDAP("LDAP", AuthenticationMode.USERNAME_PASSWORD, true, false),
    AD("AD", AuthenticationMode.USERNAME_PASSWORD, false, false),
    CUSTOM("CUSTOM", AuthenticationMode.USERNAME_PASSWORD, false, false),
    OPENID("OPENID", AuthenticationMode.SSO, false, false),
    SAML("SAML", AuthenticationMode.SSO, false, true),
    CAS("CAS", AuthenticationMode.SSO, false, false),
    JWS("JWS", AuthenticationMode.PRE_AUTHENTICATED, false, false),
    OPAQUE_TOKEN("OPAQUE_TOKEN", AuthenticationMode.PRE_AUTHENTICATED, false, false);

    private String providerType;

    private AuthenticationMode authenticationMode;

    private boolean rememberMeSupport;

    private boolean browserRedirectLogout;

    SecurityProviders(String providerType, AuthenticationMode authenticationMode, boolean rememberMeSupport, boolean browserRedirectLogout) {
        this.providerType = providerType;
        this.authenticationMode = authenticationMode;
        this.rememberMeSupport = rememberMeSupport;
        this.browserRedirectLogout = browserRedirectLogout;
    }

    public String getProviderType() {
        return providerType;
    }

    public AuthenticationMode getAuthenticationMode() {
        return authenticationMode;
    }

    public boolean isRememberMeSupported() {
        return rememberMeSupport;
    }

    public boolean isBrowserRedirectLogout() {
        return browserRedirectLogout;
    }

    public static Set<String> getProviders() {
        Set<String> providers = new HashSet<>();
        for (SecurityProviders s : values()) {
            providers.add(s.getProviderType());
        }
        return providers;
    }

    public static Stream<SecurityProviders> getSSOProviders() {
        return Arrays.stream(values()).filter(securityProviders -> securityProviders.getAuthenticationMode() == AuthenticationMode.SSO);
    }

    public static Stream<SecurityProviders> getUsernamePasswordFlowProviders() {
        return Arrays.stream(values()).filter(securityProviders -> securityProviders.getAuthenticationMode() == AuthenticationMode.USERNAME_PASSWORD);
    }

    public static Stream<SecurityProviders> getRememberMeSupportedProviders() {
        return Arrays.stream(values()).filter(SecurityProviders::isRememberMeSupported);
    }

    public static Stream<SecurityProviders> getBrowserRedirectLogoutSupportedProviders() {
        return Arrays.stream(values()).filter(SecurityProviders::isBrowserRedirectLogout);
    }
}
