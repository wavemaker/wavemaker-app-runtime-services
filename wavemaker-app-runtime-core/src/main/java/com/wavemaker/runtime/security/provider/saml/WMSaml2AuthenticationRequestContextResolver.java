package com.wavemaker.runtime.security.provider.saml;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestContext;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestContextResolver;
import org.springframework.util.Assert;

import com.wavemaker.commons.WMRuntimeException;

public class WMSaml2AuthenticationRequestContextResolver implements Saml2AuthenticationRequestContextResolver {

    private static final Logger logger = LoggerFactory.getLogger(WMSaml2AuthenticationRequestContextResolver.class);
    private final Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver;

    public WMSaml2AuthenticationRequestContextResolver(
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
        logger.debug("Request URL is {}", requestURL.toString());

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