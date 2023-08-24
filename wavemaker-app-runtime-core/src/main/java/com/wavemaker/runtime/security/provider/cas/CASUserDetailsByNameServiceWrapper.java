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
package com.wavemaker.runtime.security.provider.cas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.wavemaker.app.security.models.config.cas.CASProviderConfig;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;

/**
 * Created by ArjunSahasranam on 5/16/16.
 */
public class CASUserDetailsByNameServiceWrapper implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    @Value("${security.providers.cas.roleAttributeName:#{null}}")
    private String roleAttributeName;

    @Autowired(required = false)
    private AuthoritiesProvider authoritiesProvider;

    private CASProviderConfig casProviderConfig;

    public CASUserDetailsByNameServiceWrapper(CASProviderConfig casProviderConfig) {
        this.casProviderConfig = casProviderConfig;
    }

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken casAssertionAuthenticationToken) throws UsernameNotFoundException {
        List<GrantedAuthority> grantedAuthorities = resolveGrantedAuthorities(casAssertionAuthenticationToken);
        return new User(casAssertionAuthenticationToken.getAssertion().getPrincipal().getName(), "NO_PASSWORD", true, true, true, true,
            grantedAuthorities);
    }

    private List<GrantedAuthority> resolveGrantedAuthorities(CasAssertionAuthenticationToken casAssertionAuthenticationToken) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        boolean roleMappingEnabled = casProviderConfig.isRoleMappingEnabled();
        String roleProvider = casProviderConfig.getRoleProvider();
        if (roleMappingEnabled && StringUtils.isNotBlank(roleProvider)) {
            if (authoritiesProvider != null) {
                grantedAuthorities.addAll(authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(casAssertionAuthenticationToken.getName())));
            } else if (StringUtils.isNotBlank(roleAttributeName)) {
                Map attributes = casAssertionAuthenticationToken.getAssertion().getPrincipal().getAttributes();
                String roles = (String) attributes.get(roleAttributeName);
                StringTokenizer roleTokenizer = new StringTokenizer(roles, ",");
                List<String> rolesList = new ArrayList<>();
                while (roleTokenizer.hasMoreTokens()) {
                    String role = roleTokenizer.nextToken();
                    rolesList.add(role);
                }
                String[] rolesArray = new String[rolesList.size()];
                grantedAuthorities.addAll(AuthorityUtils.createAuthorityList(rolesList.toArray(rolesArray)));
            }
        }
        return grantedAuthorities;
    }
}
