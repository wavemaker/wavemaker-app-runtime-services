package com.wavemaker.runtime.security.provider.saml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.wavemaker.commons.WMRuntimeException;

public class WMRelyingPartyRegistrationResolver implements Converter<HttpServletRequest, RelyingPartyRegistration>, RelyingPartyRegistrationResolver {

    private static final char PATH_DELIMITER = '/';
    private static final Logger logger = LoggerFactory.getLogger(WMRelyingPartyRegistrationResolver.class);
    private final RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;
    private final RequestMatcher registrationRequestMatcher = new AntPathRequestMatcher("/**/{registrationId}");
    private String applicationUri;

    public WMRelyingPartyRegistrationResolver(
            RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
        Assert.notNull(relyingPartyRegistrationRepository, "relyingPartyRegistrationRepository cannot be null");
        this.relyingPartyRegistrationRepository = relyingPartyRegistrationRepository;
    }

    @Override
    public RelyingPartyRegistration convert(HttpServletRequest request) {
        return resolve(request, null);
    }

    private Function<String, String> templateResolver(String applicationUri, RelyingPartyRegistration relyingParty) {
        return (template) -> resolveUrlTemplate(template, applicationUri, relyingParty);
    }

    private static String resolveUrlTemplate(String template, String baseUrl, RelyingPartyRegistration relyingParty) {
        String entityId = relyingParty.getAssertingPartyDetails().getEntityId();
        String registrationId = relyingParty.getRegistrationId();
        Map<String, String> uriVariables = new HashMap<>();
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUrl).replaceQuery(null).fragment(null)
                .build();
        String scheme = uriComponents.getScheme();
        uriVariables.put("baseScheme", (scheme != null) ? scheme : "");
        String host = uriComponents.getHost();
        uriVariables.put("baseHost", (host != null) ? host : "");
        // following logic is based on HierarchicalUriComponents#toUriString()
        int port = uriComponents.getPort();
        uriVariables.put("basePort", (port == -1) ? "" : ":" + port);
        String path = uriComponents.getPath();
        if (StringUtils.hasLength(path) && path.charAt(0) != PATH_DELIMITER) {
            path = PATH_DELIMITER + path;
        }
        uriVariables.put("basePath", (path != null) ? path : "");
        uriVariables.put("baseUrl", uriComponents.toUriString());
        uriVariables.put("entityId", StringUtils.hasText(entityId) ? entityId : "");
        uriVariables.put("registrationId", StringUtils.hasText(registrationId) ? registrationId : "");
        return UriComponentsBuilder.fromUriString(template).buildAndExpand(uriVariables).toUriString();
    }

    @Override
    public RelyingPartyRegistration resolve(HttpServletRequest request, String relyingPartyRegistrationId) {
        if (relyingPartyRegistrationId == null) {
            relyingPartyRegistrationId = this.registrationRequestMatcher.matcher(request).getVariables()
                    .get("registrationId");
        }
        RelyingPartyRegistration relyingPartyRegistration = this.relyingPartyRegistrationRepository
                .findByRegistrationId(relyingPartyRegistrationId);
        if (relyingPartyRegistration == null) {
            return null;
        }
        if (applicationUri == null) {
            applicationUri = getAppUrl(request);
        }
        Function<String, String> templateResolver = templateResolver(applicationUri, relyingPartyRegistration);
        String relyingPartyEntityId = templateResolver.apply(relyingPartyRegistration.getEntityId());
        String assertionConsumerServiceLocation = templateResolver
                .apply(relyingPartyRegistration.getAssertionConsumerServiceLocation());
        String singleLogoutServiceLocation = templateResolver
                .apply(relyingPartyRegistration.getSingleLogoutServiceLocation());
        String singleLogoutServiceResponseLocation = templateResolver
                .apply(relyingPartyRegistration.getSingleLogoutServiceResponseLocation());
        return RelyingPartyRegistration.withRelyingPartyRegistration(relyingPartyRegistration)
                .entityId(relyingPartyEntityId).assertionConsumerServiceLocation(assertionConsumerServiceLocation)
                .singleLogoutServiceLocation(singleLogoutServiceLocation)
                .singleLogoutServiceResponseLocation(singleLogoutServiceResponseLocation).build();
    }

    public void setApplicationUri(String applicationUri) {
        this.applicationUri = applicationUri;
    }

    private String getAppUrl(HttpServletRequest request) {
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
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(redirectPage) && org.apache.commons.lang3.StringUtils.isNotEmpty(appUrl) && !org.apache.commons.lang3.StringUtils
                    .containsAny(appUrl, '#', '?')) {
                appUrl = appUrl.concat("#").concat(redirectPage);
            }
            return appUrl;
        } catch (MalformedURLException e) {
            logger.error("Invalid URL {}", requestURL, e);
            throw new WMRuntimeException("Invalid URL {}", e);
        }
    }
}
