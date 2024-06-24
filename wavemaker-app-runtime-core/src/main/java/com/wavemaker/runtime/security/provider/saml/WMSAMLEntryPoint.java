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

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.security.entrypoint.SSOEntryPoint;

import static com.wavemaker.runtime.security.SecurityConstants.SESSION_NOT_FOUND;
import static com.wavemaker.runtime.security.SecurityConstants.X_WM_LOGIN_ERROR_MESSAGE;

/**
 * Created by ArjunSahasranam on 27/10/16.
 */
public class WMSAMLEntryPoint extends LoginUrlAuthenticationEntryPoint implements SSOEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(WMSAMLEntryPoint.class);

    public WMSAMLEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final AuthenticationException e) throws IOException, ServletException {
        if (HttpRequestUtils.isAjaxRequest(request)) {
            response.setHeader(X_WM_LOGIN_ERROR_MESSAGE, SESSION_NOT_FOUND);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            super.commence(request, response, e);
        }
    }
}
