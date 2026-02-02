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

package com.wavemaker.runtime.security.provider.openid;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import com.wavemaker.commons.auth.oauth2.OAuth2Helper;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.security.filter.WMRequestResponseHolderFilter;
import com.wavemaker.runtime.security.provider.openid.util.OpenIdUtils;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpenIdAuthorizationRequestCustomizer implements Consumer<OAuth2AuthorizationRequest.Builder> {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public void accept(OAuth2AuthorizationRequest.Builder builder) {
        String registrationId = builder.build().getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Invalid Client Registration with Id: " + registrationId);
        }

        HttpServletRequest request = WMRequestResponseHolderFilter.getCurrentThreadHttpServletRequest();
        String appPath = HttpRequestUtils.getApplicationBaseUrl(request);
        String redirectUriStr = OpenIdUtils.getRedirectUri(clientRegistration.getRegistrationId(), appPath);

        Map<String, String> stateObject = new HashMap<>();
        if (RuntimeEnvironment.isTestRunEnvironment()) {
            stateObject.put(OpenIdConstants.APP_PATH, appPath);
            stateObject.put(OpenIdConstants.REGISTRATION_ID_URI_VARIABLE_NAME, clientRegistration.getRegistrationId());
            stateObject.put(OpenIdConstants.REDIRECT_URI, redirectUriStr);
        }
        if (StringUtils.isNotEmpty(request.getParameter(OpenIdConstants.REDIRECT_PAGE))) {
            stateObject.put(OpenIdConstants.REDIRECT_PAGE, request.getParameter(OpenIdConstants.REDIRECT_PAGE));
        }
        String encodedState = OAuth2Helper.getStateParameterValue(stateObject);

        builder
            .redirectUri(redirectUriStr)
            .state(encodedState);
    }

}