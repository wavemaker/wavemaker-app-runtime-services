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
package com.wavemaker.runtime.security.provider.cas.handler;

import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAuthenticationToken;

import com.wavemaker.runtime.security.Attribute;
import com.wavemaker.runtime.security.WMAuthentication;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.model.AuthProviderType;

public class WMCasAuthenticationSuccessHandler implements WMAuthenticationSuccessHandler {

    private Logger logger = LoggerFactory.getLogger(WMCasAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, WMAuthentication authentication) {
        if (Objects.equals(authentication.getAuthProviderType(), AuthProviderType.CAS)) {
            CasAuthenticationToken casAuthentication = (CasAuthenticationToken) authentication.getAuthenticationSource();
            Map<String, Object> attributes = casAuthentication.getAssertion().getPrincipal().getAttributes();
            logger.debug("Cas authentication user attributes : {}", attributes);
            attributes.forEach((key, value) -> {
                authentication.addAttribute(key, value, Attribute.AttributeScope.ALL);
            });
        }
    }
}
