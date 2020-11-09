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
package com.wavemaker.runtime.security.provider.ldap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;

import com.wavemaker.runtime.security.provider.ad.WMAdAuthentication;

public abstract class WMAbstractLdapAuthenticationProvider extends AbstractLdapAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication authenticate = super.authenticate(authentication);
        Object tenantId = getTenantId(authenticate.getName());
        if (tenantId != null) {
            WMAdAuthentication wmAdAuthentication = (WMAdAuthentication) authenticate;
            wmAdAuthentication.setTenantId(tenantId);
            return wmAdAuthentication;
        }else {
            return authenticate;
        }
    }

    public abstract Object getTenantId(String username);

}
