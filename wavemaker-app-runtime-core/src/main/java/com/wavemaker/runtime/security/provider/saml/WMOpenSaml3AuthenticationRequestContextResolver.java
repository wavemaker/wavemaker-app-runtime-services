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

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestContext;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestContextResolver;
import org.springframework.util.Assert;

import com.wavemaker.commons.WMRuntimeException;

public class WMOpenSaml3AuthenticationRequestContextResolver implements Saml2AuthenticationRequestContextResolver {

    private static final Logger logger = LoggerFactory.getLogger(WMOpenSaml3AuthenticationRequestContextResolver.class);
    private final Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver;

    public WMOpenSaml3AuthenticationRequestContextResolver(
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver) {
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
    }

    @Override
    public Saml2AuthenticationRequestContext resolve(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        RelyingPartyRegistration relyingParty = this.relyingPartyRegistrationResolver.convert(request);
        if (relyingParty == null) {
            return null;
        }

        StringBuffer requestURL = request.getRequestURL();
        logger.debug("Request URL is {}", requestURL);

        try {
            URL incomingRequestUrl = new URL(requestURL.toString());
            String incomingRequestUrlPath = incomingRequestUrl.getPath(); //content after port,
            // excluding the query string, but starts with slash (/)

            int indexOfPath = requestURL.indexOf(incomingRequestUrlPath);
            StringBuffer requestUrlBeforePath = requestURL.delete(indexOfPath, requestURL.length());

            String appUrl = requestUrlBeforePath.toString().concat(request.getContextPath());
            logger.debug("URL incomingRequestUrlPath constructed for application is {}", appUrl);

            String redirectPage = request.getParameter("redirectPage");
            if (StringUtils.isNotEmpty(redirectPage) && StringUtils.isNotEmpty(appUrl) && !StringUtils
                .containsAny(appUrl, '#', '?')) {
                appUrl = appUrl.concat("#").concat(redirectPage);
            }

            return Saml2AuthenticationRequestContext.builder().issuer(relyingParty.getEntityId())
                .relyingPartyRegistration(relyingParty)
                .assertionConsumerServiceUrl(relyingParty.getAssertionConsumerServiceLocation())
                .relayState(appUrl).build();

        } catch (MalformedURLException e) {
            logger.error("Invalid URL {}", requestURL, e);
            throw new WMRuntimeException(e);
        }
    }

}
