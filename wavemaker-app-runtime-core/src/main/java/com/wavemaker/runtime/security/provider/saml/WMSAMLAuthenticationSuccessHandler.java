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

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SAMLRelayStateSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;

import com.wavemaker.runtime.util.HttpRequestUtils;

/**
 * @author Kishore Routhu on 1/2/18 11:22 AM.
 */
public class WMSAMLAuthenticationSuccessHandler extends SAMLRelayStateSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        Optional<CsrfToken> csrfTokenOptional = HttpRequestUtils.getCsrfToken(request);
        HttpRequestUtils.addCsrfCookie(csrfTokenOptional, request, response);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
