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

package com.wavemaker.runtime.security.provider.cas.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.wavemaker.commons.util.HttpRequestUtils;

public class WMCasAuthenticationFilter extends CasAuthenticationFilter {

    @Value("${security.providers.cas.proxyCallBackUrl}")
    private String proxyCallBackUrl;
    @Autowired
    private Cas20ProxyTicketValidator cas20ProxyTicketValidator;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (!StringUtils.isBlank(proxyCallBackUrl)) {
            cas20ProxyTicketValidator.setProxyCallbackUrl(proxyCallBackUrl);
        } else {
            cas20ProxyTicketValidator.setProxyCallbackUrl(HttpRequestUtils.getApplicationBaseUrl(request) + "/j_spring_cas_security_proxyreceptor");
        }
        return super.attemptAuthentication(request, response);
    }
}
