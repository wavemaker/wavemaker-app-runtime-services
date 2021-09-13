package com.wavemaker.runtime.security.provider.saml.logout;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml.saml2.core.impl.LogoutRequestMarshaller;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.WMAuthentication;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

public class OpenSamlLogoutRequestResolver {

    static {
        OpenSamlInitializationService.initialize();
    }

    private final Log logger = LogFactory.getLog(getClass());

    private final LogoutRequestMarshaller marshaller;

    private final IssuerBuilder issuerBuilder;

    private final NameIDBuilder nameIdBuilder;

    private final LogoutRequestBuilder logoutRequestBuilder;

    private final RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;

    OpenSamlLogoutRequestResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
        XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
        this.marshaller = (LogoutRequestMarshaller) registry.getMarshallerFactory()
                .getMarshaller(LogoutRequest.DEFAULT_ELEMENT_NAME);
        Assert.notNull(this.marshaller, "logoutRequestMarshaller must be configured in OpenSAML");
        this.logoutRequestBuilder = (LogoutRequestBuilder) registry.getBuilderFactory()
                .getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
        Assert.notNull(this.logoutRequestBuilder, "logoutRequestBuilder must be configured in OpenSAML");
        this.issuerBuilder = (IssuerBuilder) registry.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Assert.notNull(this.issuerBuilder, "issuerBuilder must be configured in OpenSAML");
        this.nameIdBuilder = (NameIDBuilder) registry.getBuilderFactory().getBuilder(NameID.DEFAULT_ELEMENT_NAME);
        Assert.notNull(this.nameIdBuilder, "nameIdBuilder must be configured in OpenSAML");
    }

    /**
     * Prepare to create, sign, and serialize a SAML 2.0 Logout Request.
     * <p>
     * By default, includes a {@code NameID} based on the {@link Authentication} instance
     * as well as the {@code Destination} and {@code Issuer} based on the
     * {@link RelyingPartyRegistration} derived from the {@link Authentication}.
     *
     * @param request        the HTTP request
     * @param authentication the current user
     * @return a signed and serialized SAML 2.0 Logout Request
     */
    Saml2LogoutRequest resolve(HttpServletRequest request, Authentication authentication) {
        return resolve(request, authentication, (registration, logoutRequest) -> {
        });
    }

    Saml2LogoutRequest resolve(HttpServletRequest request, Authentication authentication,
                               BiConsumer<RelyingPartyRegistration, LogoutRequest> logoutRequestConsumer) {
        String registrationId = getRegistrationId(authentication);
        RelyingPartyRegistration registration = this.relyingPartyRegistrationResolver.resolve(request, registrationId);
        if (registration == null) {
            return null;
        }
        LogoutRequest logoutRequest = this.logoutRequestBuilder.buildObject();
        logoutRequest.setDestination(registration.getAssertingPartyDetails().getSingleLogoutServiceLocation());
        Issuer issuer = this.issuerBuilder.buildObject();
        issuer.setValue(registration.getEntityId());
        logoutRequest.setIssuer(issuer);
        NameID nameId = this.nameIdBuilder.buildObject();
        nameId.setValue(authentication.getName());
        logoutRequest.setNameID(nameId);
        logoutRequestConsumer.accept(registration, logoutRequest);
        if (logoutRequest.getID() == null) {
            logoutRequest.setID("LR" + UUID.randomUUID());
        }
        String relayState = getRelayState(request);
        Saml2LogoutRequest.Builder result = Saml2LogoutRequest.withRelyingPartyRegistration(registration)
                .id(logoutRequest.getID());
        if (registration.getAssertingPartyDetails().getSingleLogoutServiceBinding() == Saml2MessageBinding.POST) {
            String xml = serialize(OpenSamlSigningUtils.sign(logoutRequest, registration));
            String samlRequest = Saml2Utils.samlEncode(xml.getBytes(StandardCharsets.UTF_8));
            return result.samlRequest(samlRequest).relayState(relayState).build();
        } else {
            String xml = serialize(logoutRequest);
            String deflatedAndEncoded = Saml2Utils.samlEncode(Saml2Utils.samlDeflate(xml));
            result.samlRequest(deflatedAndEncoded);
            OpenSamlSigningUtils.QueryParametersPartial partial = OpenSamlSigningUtils.sign(registration)
                    .param(Saml2ParameterNames.SAML_REQUEST, deflatedAndEncoded)
                    .param(Saml2ParameterNames.RELAY_STATE, relayState);
            return result.parameters((params) -> params.putAll(partial.parameters())).build();
        }
    }

    private String getRegistrationId(Authentication authentication) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Attempting to resolve registrationId from " + authentication);
        }
        if (authentication == null) {
            return null;
        }
        if (authentication instanceof WMAuthentication) {
            WMAuthentication wmAuthentication = (WMAuthentication)authentication;
            Object principal = wmAuthentication.getAuthenticationSource().getPrincipal();
            if (principal instanceof Saml2AuthenticatedPrincipal) {
                return ((Saml2AuthenticatedPrincipal) principal).getRelyingPartyRegistrationId();
            }
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Saml2AuthenticatedPrincipal) {
            return ((Saml2AuthenticatedPrincipal) principal).getRelyingPartyRegistrationId();
        }
        return null;
    }

    private String serialize(LogoutRequest logoutRequest) {
        try {
            Element element = this.marshaller.marshall(logoutRequest);
            return SerializeSupport.nodeToString(element);
        } catch (MarshallingException ex) {
            throw new Saml2Exception(ex);
        }
    }

    private String getRelayState(HttpServletRequest request) {
        try {
            StringBuffer requestURL = request.getRequestURL();
            URL incomingRequestUrl = new URL(requestURL.toString());
            String incomingRequestUrlPath = incomingRequestUrl.getPath(); //content after port,
            // excluding the query string, but starts with slash (/)

            int indexOfPath = requestURL.indexOf(incomingRequestUrlPath);
            StringBuffer requestUrlBeforePath = requestURL.delete(indexOfPath, requestURL.length());

            String appUrl = requestUrlBeforePath.toString().concat(request.getContextPath());

            String redirectPage = request.getParameter("redirectPage");
            if (StringUtils.isNotEmpty(redirectPage) && StringUtils.isNotEmpty(appUrl) && !StringUtils
                    .containsAny(appUrl, '#', '?')) {
                appUrl = appUrl.concat("#").concat(redirectPage);
            }
            return appUrl;
        } catch (MalformedURLException e) {
            throw new WMRuntimeException(e);
        }

    }
}
