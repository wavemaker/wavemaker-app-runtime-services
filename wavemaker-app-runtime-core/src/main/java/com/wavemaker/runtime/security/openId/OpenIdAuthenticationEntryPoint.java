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
package com.wavemaker.runtime.security.openId;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.security.core.AuthenticationException;

import com.wavemaker.runtime.security.entrypoint.SSOEntryPoint;
import com.wavemaker.runtime.util.HttpRequestUtils;

import static com.wavemaker.runtime.security.SecurityConstants.SESSION_NOT_FOUND;
import static com.wavemaker.runtime.security.SecurityConstants.X_WM_LOGIN_ERROR_MESSAGE;

/**
 * Authentication entryPoint to redirect application to {@link OpenIDAuthorizationRequestRedirectFilter}.
 * Request is commenced to this entryPoint if OpenId security is configured in the application.
 *
 * Created by srujant on 2/8/18.
 */
public class OpenIdAuthenticationEntryPoint implements SSOEntryPoint {

    @NotNull
    private String providerId;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (HttpRequestUtils.isAjaxRequest(request)) {
            response.setHeader(X_WM_LOGIN_ERROR_MESSAGE, SESSION_NOT_FOUND);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            String redirectUrl = createRedirectUrl(request);
            response.sendRedirect(redirectUrl);
        }

    }

    private String createRedirectUrl(HttpServletRequest request) {
        String serviceUrl = HttpRequestUtils.getServiceUrl(request);
        return new StringBuilder(serviceUrl).append("/auth/oauth2/").append(providerId).toString();
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
