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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationConverter;

import static com.wavemaker.runtime.security.provider.saml.SAMLHttpServletRequestWrapper.EndpointType.SSO;

public class WMSaml2WebSsoAuthenticationFilter extends Saml2WebSsoAuthenticationFilter {

    @Autowired
    private SAMLConfig samlConfig;

    @Autowired
    private Saml2AuthenticationTokenConverter saml2AuthenticationTokenConverter;

    public WMSaml2WebSsoAuthenticationFilter(AuthenticationConverter authenticationConverter) {
        super(authenticationConverter, Saml2WebSsoAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        Saml2AuthenticationToken saml2AuthenticationToken = saml2AuthenticationTokenConverter.convert(request);
        if (samlConfig.getValidateType() == SAMLConfig.ValidateType.RELAXED) {
            SAMLHttpServletRequestWrapper requestWrapper = new SAMLHttpServletRequestWrapper(request, saml2AuthenticationToken, SSO);
            return super.attemptAuthentication(requestWrapper, response);
        }
        return super.attemptAuthentication(request, response);
    }
}
