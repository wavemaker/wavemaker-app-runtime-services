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
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

/**
 * Created by arjuns on 18/1/17.
 */
public class SAMLHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(SAMLHttpServletRequestWrapper.class);

    private EndpointType endpointType;
    private Saml2AuthenticationToken saml2AuthenticationToken;

    public enum EndpointType {
        SSO, // Single Sign-on Service
        SLO  // Single Logout Service
    }

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null
     */
    public SAMLHttpServletRequestWrapper(HttpServletRequest request, Saml2AuthenticationToken saml2AuthenticationToken, EndpointType endpointType) {
        super(request);
        this.saml2AuthenticationToken = saml2AuthenticationToken;
        this.endpointType = endpointType;
    }

    @Override
    public StringBuffer getRequestURL() {
        String endPoint = null;
        if (EndpointType.SSO == this.endpointType) {
            endPoint = saml2AuthenticationToken.getRelyingPartyRegistration().getAssertionConsumerServiceLocation();
        } else {
            //TODO for SLO
            endPoint = saml2AuthenticationToken.getRelyingPartyRegistration().getSingleLogoutServiceLocation();
        }
        logger.debug("Endpoint is {} and Url is {}", endpointType, endPoint);
        return new StringBuffer(endPoint);
    }

}
