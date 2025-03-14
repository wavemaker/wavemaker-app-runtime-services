/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.security.authenticationprovider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.wavemaker.runtime.security.constants.SecurityConstants;
import com.wavemaker.runtime.security.filter.WMRequestResponseHolderFilter;
import com.wavemaker.runtime.security.model.AuthProviderType;

public class WMDelegatingAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticationProvider delegate;

    private final AuthProviderType authProviderType;

    public WMDelegatingAuthenticationProvider(AuthenticationProvider authenticationProvider, AuthProviderType authProviderType) {
        this.delegate = authenticationProvider;
        this.authProviderType = authProviderType;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication authenticate = delegate.authenticate(authentication);
        if (authenticate != null) {
            WMRequestResponseHolderFilter.getCurrentThreadHttpServletRequest().setAttribute(SecurityConstants.PROVIDER_TYPE, authProviderType);
        }
        return authenticate;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return this.delegate.supports(authentication);
    }
}
