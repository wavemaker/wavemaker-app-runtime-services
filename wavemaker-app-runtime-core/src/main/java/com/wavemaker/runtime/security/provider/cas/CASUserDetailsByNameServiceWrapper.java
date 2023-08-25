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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;

/**
 * Created by ArjunSahasranam on 5/16/16.
 */
public class CASUserDetailsByNameServiceWrapper implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    @Autowired
    @Qualifier("casAuthoritiesProvider")
    private AuthoritiesProvider authoritiesProvider;

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken casAssertionAuthenticationToken) throws UsernameNotFoundException {
        List<GrantedAuthority> grantedAuthorities = authoritiesProvider.loadAuthorities(new CASAuthenticationContext(casAssertionAuthenticationToken,
            casAssertionAuthenticationToken.getName()));
        return new User(casAssertionAuthenticationToken.getAssertion().getPrincipal().getName(), "NO_PASSWORD", true,
            true, true, true, grantedAuthorities);
    }
}
