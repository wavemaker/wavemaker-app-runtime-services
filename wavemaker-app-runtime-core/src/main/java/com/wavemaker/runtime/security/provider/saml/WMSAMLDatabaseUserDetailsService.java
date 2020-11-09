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
package com.wavemaker.runtime.security.provider.saml;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import com.wavemaker.runtime.security.WMUser;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;
import com.wavemaker.runtime.security.core.TenantIdProvider;

/**
 * @author Arjun Sahasranam
 */
public class WMSAMLDatabaseUserDetailsService implements SAMLUserDetailsService {

    private AuthoritiesProvider authoritiesProvider;
    private TenantIdProvider tenantIdProvider;

    public WMSAMLDatabaseUserDetailsService() {
    }

    @Override
    public Object loadUserBySAML(final SAMLCredential credential) {
        String username = credential.getNameID().getValue();
        Set<GrantedAuthority> dbAuthsSet = new HashSet<>();

        dbAuthsSet.addAll(authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(username)));

        long loginTime = System.currentTimeMillis();
        if (tenantIdProvider != null) {
            Object tenantId = tenantIdProvider.loadTenantId(new DefaultAuthenticationContext(username));
            return new WMUser("", username, "", username, tenantId, true, true, true, true, dbAuthsSet, loginTime);
        }
        return new WMUser("", username, "", username, null, true, true, true, true, dbAuthsSet, loginTime);
    }

    public AuthoritiesProvider getAuthoritiesProvider() {
        return authoritiesProvider;
    }

    public void setAuthoritiesProvider(AuthoritiesProvider authoritiesProvider) {
        this.authoritiesProvider = authoritiesProvider;
    }

    public TenantIdProvider getTenantIdProvider() {
        return tenantIdProvider;
    }

    public void setTenantIdProvider(TenantIdProvider tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }
}
