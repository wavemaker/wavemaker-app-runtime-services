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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider.ResponseToken;
import org.springframework.util.CollectionUtils;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;

/**
 * @author Arjun Sahasranam
 */
public class WMSAMLDatabaseUserDetailsService implements SAMLUserDetailsService {

    private AuthoritiesProvider authoritiesProvider;

    public WMSAMLDatabaseUserDetailsService() {
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities(ResponseToken responseToken) {
        Response response = responseToken.getResponse();
        Assertion assertion = CollectionUtils.firstElement(response.getAssertions());
        String username = assertion.getSubject().getNameID().getValue();
        Set<GrantedAuthority> dbAuthsSet = new HashSet<>();
        dbAuthsSet.addAll(authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(username)));
        return dbAuthsSet;
    }

    public AuthoritiesProvider getAuthoritiesProvider() {
        return authoritiesProvider;
    }

    public void setAuthoritiesProvider(AuthoritiesProvider authoritiesProvider) {
        this.authoritiesProvider = authoritiesProvider;
    }

}
