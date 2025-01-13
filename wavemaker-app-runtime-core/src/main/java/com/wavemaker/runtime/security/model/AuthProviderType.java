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

package com.wavemaker.runtime.security.model;

public enum AuthProviderType {

    DEMO(AuthenticationMode.USERNAME_PASSWORD, false, true, false),
    DATABASE(AuthenticationMode.USERNAME_PASSWORD, false, true, false),
    LDAP(AuthenticationMode.USERNAME_PASSWORD, false, true, false),
    AD(AuthenticationMode.USERNAME_PASSWORD, false, false, false),
    CUSTOM(AuthenticationMode.USERNAME_PASSWORD, false, false, false),
    OPENID(AuthenticationMode.SSO, true, true, false),
    SAML(AuthenticationMode.SSO, false, false, true),
    CAS(AuthenticationMode.SSO, false, false, false),
    JWS(AuthenticationMode.PRE_AUTHENTICATED, true, false, false),
    OPAQUE_TOKEN(AuthenticationMode.PRE_AUTHENTICATED, false, false, false);

    private final AuthenticationMode authenticationMode;
    private final boolean multiInstance;
    private final boolean rememberMeSupported;
    private final boolean browserRedirectLogout;

    AuthProviderType(AuthenticationMode authenticationMode, boolean multiInstance, boolean rememberMeSupported, boolean browserRedirectLogout) {
        this.authenticationMode = authenticationMode;
        this.multiInstance = multiInstance;
        this.rememberMeSupported = rememberMeSupported;
        this.browserRedirectLogout = browserRedirectLogout;
    }

    public AuthenticationMode getAuthenticationMode() {
        return authenticationMode;
    }

    public boolean isMultiInstance() {
        return multiInstance;
    }

    public boolean isRememberMeSupported() {
        return rememberMeSupported;
    }

    public boolean isBrowserRedirectLogout() {
        return browserRedirectLogout;
    }
}
