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
package com.wavemaker.runtime.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.wavemaker.runtime.web.filter.WMRequestFilter;

/**
 * @author Uday Shankar
 */
public class WMCustomAuthenticationProvider implements AuthenticationProvider {

    private WMCustomAuthenticationManager wmCustomAuthenticationManager;

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (authentication instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
            String username = (String) usernamePasswordAuthenticationToken.getPrincipal();
            String password = (String) usernamePasswordAuthenticationToken.getCredentials();
            HttpServletRequest httpServletRequest = WMRequestFilter.getCurrentThreadHttpServletRequest();
            AuthRequestContext authRequestContext = new AuthRequestContext(username, password, httpServletRequest);
            try {
                WMUser wmUser = wmCustomAuthenticationManager.authenticate(authRequestContext);
                if (wmUser == null) {
                    throw new BadCredentialsException("Invalid credentials");
                }
                return new UsernamePasswordAuthenticationToken(wmUser, null, wmUser.getAuthorities());
            } catch (AuthenticationException e) {
                throw e;
            } catch (Exception e) {
                throw new AuthenticationServiceException("Error while authenticating user", e);
            }
        } else {
            throw new IllegalArgumentException(
                "Authentication type of class [" + authentication.getClass() + "] is not supported by this class");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setWmCustomAuthenticationManager(WMCustomAuthenticationManager wmCustomAuthenticationManager) {
        this.wmCustomAuthenticationManager = wmCustomAuthenticationManager;
    }
}
