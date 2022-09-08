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
package com.wavemaker.runtime.security.provider.saml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;

/**
 * @author Arjun Sahasranam
 */
public class SamlDatabaseBasedAuthoritiesExtractor implements Converter<Assertion, Collection<? extends GrantedAuthority>> {

    @Autowired
    private AuthoritiesProvider authoritiesProvider;

    @Override
    public Collection<GrantedAuthority> convert(Assertion assertion) {
        String username = assertion.getSubject().getNameID().getValue();
        Set<GrantedAuthority> dbAuthsSet = new HashSet<>();
        dbAuthsSet.addAll(authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(username)));
        return dbAuthsSet;
    }

}
