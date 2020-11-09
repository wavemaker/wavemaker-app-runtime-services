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

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import com.wavemaker.runtime.security.WMUser;
import com.wavemaker.runtime.security.WMUserDetails;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;
import com.wavemaker.runtime.security.core.TenantIdProvider;

public class LdapUserDetailsService extends org.springframework.security.ldap.userdetails.LdapUserDetailsService {

    private TenantIdProvider tenantIdProvider;

    public LdapUserDetailsService(LdapUserSearch userSearch) {
        super(userSearch);
    }

    public LdapUserDetailsService(LdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator) {
        super(userSearch, authoritiesPopulator);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = super.loadUserByUsername(username);
        WMUserDetails wmUserDetails = (WMUserDetails) userDetails;
        Object tenantId = null;
        if (tenantIdProvider != null) {
            tenantId = tenantIdProvider.loadTenantId(new DefaultAuthenticationContext(username));
        }
        return new WMUser(wmUserDetails.getUserId(), wmUserDetails.getUsername(), wmUserDetails.getPassword(),
                wmUserDetails.getUserLongName(),
                tenantId, wmUserDetails.isEnabled(), true, true, true, wmUserDetails.getAuthorities(),
                wmUserDetails.getLoginTime());
    }

    public TenantIdProvider getTenantIdProvider() {
        return tenantIdProvider;
    }

    public void setTenantIdProvider(TenantIdProvider tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }
}
