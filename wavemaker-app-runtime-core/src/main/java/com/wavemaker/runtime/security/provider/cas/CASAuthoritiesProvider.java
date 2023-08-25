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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.wavemaker.runtime.security.core.AuthenticationContext;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;

public class CASAuthoritiesProvider implements AuthoritiesProvider {

    private static final Logger logger = LoggerFactory.getLogger(CASAuthoritiesProvider.class);

    private String roleAttributeName;

    @Override
    public List<GrantedAuthority> loadAuthorities(AuthenticationContext authenticationContext) {
        if (StringUtils.isBlank(roleAttributeName)) {
            logger.warn("CAS role attribute is not configured");
            return AuthorityUtils.NO_AUTHORITIES;
        }
        CASAuthenticationContext casAuthenticationContext = (CASAuthenticationContext) authenticationContext;
        CasAssertionAuthenticationToken casAssertionAuthenticationToken = casAuthenticationContext.getCasAssertionAuthenticationToken();
        Map attributes = casAssertionAuthenticationToken.getAssertion().getPrincipal().getAttributes();
        String roles = (String) attributes.get(roleAttributeName);
        StringTokenizer roleTokenizer = new StringTokenizer(roles, ",");
        List<String> rolesList = new ArrayList<>();
        while (roleTokenizer.hasMoreTokens()) {
            String role = roleTokenizer.nextToken();
            rolesList.add(role);
        }
        String[] rolesArray = new String[rolesList.size()];
        return new ArrayList<>(AuthorityUtils.createAuthorityList(rolesList.toArray(rolesArray)));
    }

    public void setRoleAttributeName(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }
}
