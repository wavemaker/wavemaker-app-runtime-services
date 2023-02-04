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

package com.wavemaker.runtime.security.jws;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;

public class JWSDatabaseGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private String userNameClaimName;

    private AuthoritiesProvider authoritiesProvider;

    public JWSDatabaseGrantedAuthoritiesConverter(String userNameClaimName, AuthoritiesProvider authoritiesProvider) {
        this.userNameClaimName = userNameClaimName;
        this.authoritiesProvider = authoritiesProvider;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String userName = jwt.getClaimAsString(userNameClaimName);
        return authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(userName));
    }

    public void setUserNameClaimName(String userNameClaimName) {
        this.userNameClaimName = userNameClaimName;
    }

    public void setAuthoritiesProvider(AuthoritiesProvider authoritiesProvider) {
        this.authoritiesProvider = authoritiesProvider;
    }
}