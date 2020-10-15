/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.security.openId;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.wavemaker.runtime.security.core.AuthenticationContext;

/**
 * Created by srujant on 8/8/18.
 */
public class OpenIdAuthenticationContext implements AuthenticationContext {
    
    private OidcUser oidcUser;
    private String username;

    public OpenIdAuthenticationContext(String username, OidcUser oidcUser) {
        this.oidcUser = oidcUser;
        this.username = username;
    }

    public OidcUser getOidcUser() {
        return oidcUser;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
