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
package com.wavemaker.runtime.security.provider.cas;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.security.entrypoint.SSOEntryPoint;

import static com.wavemaker.runtime.security.SecurityConstants.SESSION_NOT_FOUND;
import static com.wavemaker.runtime.security.SecurityConstants.X_WM_LOGIN_ERROR_MESSAGE;

/**
 * Created by ArjunSahasranam on 5/16/16.
 */
public class WMCASAuthenticationEntryPoint extends SpringCasAuthenticationEntryPoint implements SSOEntryPoint {

    @Autowired
    @Qualifier("casServiceProperties")
    private ServiceProperties serviceProperties;

    @Override
    protected String createServiceUrl(HttpServletRequest request, HttpServletResponse response) {
        if (serviceProperties.getService().equals("/")) {
            String applicationBaseUrl = HttpRequestUtils.getApplicationBaseUrl(request);
            serviceProperties.setService(applicationBaseUrl + "/j_spring_cas_security_check");
        }

        String service = this.serviceProperties.getService();
        String redirectToPage = request.getParameter("redirectPage");
        if(StringUtils.isNotEmpty(redirectToPage)) {
            try {
                service = service + "?redirectPage=" + URLEncoder.encode(redirectToPage, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.could.not.encode.redirectpage"), e);
            }
        }
        return CommonUtils.constructServiceUrl(request, response, service, null, this.serviceProperties.getServiceParameter(),
                this.serviceProperties.getArtifactParameter(), true);
    }

    @Override
    public final void commence(final HttpServletRequest servletRequest, final HttpServletResponse response,
                               final AuthenticationException authenticationException) throws IOException, ServletException {
        if (HttpRequestUtils.isAjaxRequest(servletRequest)) {
            response.setHeader(X_WM_LOGIN_ERROR_MESSAGE, SESSION_NOT_FOUND);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            final String urlEncodedService = createServiceUrl(servletRequest, response);
            final String redirectUrl = createRedirectUrl(urlEncodedService);
            preCommence(servletRequest, response);
            response.sendRedirect(redirectUrl);
        }
    }


}
