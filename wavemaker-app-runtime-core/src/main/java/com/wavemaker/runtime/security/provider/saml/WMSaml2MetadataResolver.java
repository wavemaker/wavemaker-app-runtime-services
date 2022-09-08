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

import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.impl.EntityDescriptorMarshaller;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;

public class WMSaml2MetadataResolver implements Saml2MetadataResolver {

    static {
        OpenSamlInitializationService.initialize();
    }

    private final EntityDescriptorMarshaller entityDescriptorMarshaller;

    public WMSaml2MetadataResolver() {
        this.entityDescriptorMarshaller = (EntityDescriptorMarshaller) XMLObjectProviderRegistrySupport
                .getMarshallerFactory().getMarshaller(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        Assert.notNull(this.entityDescriptorMarshaller, "entityDescriptorMarshaller cannot be null");
    }

    @Override
    public String resolve(RelyingPartyRegistration relyingPartyRegistration) {
        EntityDescriptor entityDescriptor = build(EntityDescriptor.ELEMENT_QNAME);
        entityDescriptor.setEntityID(relyingPartyRegistration.getEntityId());
        SPSSODescriptor spSsoDescriptor = buildSpSsoDescriptor(relyingPartyRegistration);
        entityDescriptor.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).add(spSsoDescriptor);
        return serialize(entityDescriptor);
    }

    private SPSSODescriptor buildSpSsoDescriptor(RelyingPartyRegistration registration) {
        SPSSODescriptor spSsoDescriptor = build(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        spSsoDescriptor.addSupportedProtocol(org.opensaml.saml.common.xml.SAMLConstants.SAML20P_NS);
        spSsoDescriptor.setWantAssertionsSigned(true);
        spSsoDescriptor.setAuthnRequestsSigned(true);
        spSsoDescriptor.getKeyDescriptors()
                .addAll(buildKeys(registration.getSigningX509Credentials(), UsageType.SIGNING));
        spSsoDescriptor.getKeyDescriptors()
                .addAll(buildKeys(registration.getDecryptionX509Credentials(), UsageType.ENCRYPTION));
        spSsoDescriptor.getSingleLogoutServices().add(buildSingleLogoutServiceWithPostBinding(registration));
        spSsoDescriptor.getSingleLogoutServices().add(buildSingleLogoutServiceWithRedirectBinding(registration));
        spSsoDescriptor.getAssertionConsumerServices().add(buildAssertionConsumerServiceWithPostBinding(registration));
        spSsoDescriptor.getAssertionConsumerServices().add(buildAssertionConsumerServiceWithArtifactBinding(registration));
        spSsoDescriptor.getNameIDFormats().add(getNameIDFormat(NameIDType.EMAIL));
        spSsoDescriptor.getNameIDFormats().add(getNameIDFormat(NameIDType.TRANSIENT));
        spSsoDescriptor.getNameIDFormats().add(getNameIDFormat(NameIDType.PERSISTENT));
        spSsoDescriptor.getNameIDFormats().add(getNameIDFormat(NameIDType.UNSPECIFIED));
        spSsoDescriptor.getNameIDFormats().add(getNameIDFormat(NameIDType.X509_SUBJECT));
        return spSsoDescriptor;
    }

    private NameIDFormat getNameIDFormat(String nameIdFormat) {
        NameIDFormat nameID = build(NameIDFormat.DEFAULT_ELEMENT_NAME);
        nameID.setFormat(nameIdFormat);
        return nameID;
    }

    private List<KeyDescriptor> buildKeys(Collection<Saml2X509Credential> credentials, UsageType usageType) {
        List<KeyDescriptor> list = new ArrayList<>();
        for (Saml2X509Credential credential : credentials) {
            KeyDescriptor keyDescriptor = buildKeyDescriptor(usageType, credential.getCertificate());
            list.add(keyDescriptor);
        }
        return list;
    }

    private KeyDescriptor buildKeyDescriptor(UsageType usageType, java.security.cert.X509Certificate certificate) {
        KeyDescriptor keyDescriptor = build(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        KeyInfo keyInfo = build(KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Certificate x509Certificate = build(X509Certificate.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = build(X509Data.DEFAULT_ELEMENT_NAME);
        try {
            x509Certificate.setValue(new String(Base64.getEncoder().encode(certificate.getEncoded())));
        } catch (CertificateEncodingException ex) {
            throw new Saml2Exception("Cannot encode certificate " + certificate.toString());
        }
        x509Data.getX509Certificates().add(x509Certificate);
        keyInfo.getX509Datas().add(x509Data);
        keyDescriptor.setUse(usageType);
        keyDescriptor.setKeyInfo(keyInfo);
        return keyDescriptor;
    }

    private AssertionConsumerService buildAssertionConsumerServiceWithPostBinding(RelyingPartyRegistration registration) {
        AssertionConsumerService assertionConsumerService = build(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        assertionConsumerService.setLocation(registration.getAssertionConsumerServiceLocation());
        assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        assertionConsumerService.setIndex(0);
        assertionConsumerService.setIsDefault(true);
        return assertionConsumerService;
    }

    private AssertionConsumerService buildAssertionConsumerServiceWithArtifactBinding(RelyingPartyRegistration registration) {
        AssertionConsumerService assertionConsumerService = build(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        assertionConsumerService.setLocation(registration.getAssertionConsumerServiceLocation());
        assertionConsumerService.setBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        assertionConsumerService.setIndex(1);
        return assertionConsumerService;
    }

    private SingleLogoutService buildSingleLogoutServiceWithPostBinding(RelyingPartyRegistration registration) {
        SingleLogoutService singleLogoutService = build(SingleLogoutService.DEFAULT_ELEMENT_NAME);
        singleLogoutService.setLocation(registration.getSingleLogoutServiceLocation());
        singleLogoutService.setResponseLocation(registration.getSingleLogoutServiceResponseLocation());
        singleLogoutService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        return singleLogoutService;
    }

    private SingleLogoutService buildSingleLogoutServiceWithRedirectBinding(RelyingPartyRegistration registration) {
        SingleLogoutService singleLogoutService = build(SingleLogoutService.DEFAULT_ELEMENT_NAME);
        singleLogoutService.setLocation(registration.getSingleLogoutServiceLocation());
        singleLogoutService.setResponseLocation(registration.getSingleLogoutServiceResponseLocation());
        singleLogoutService.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        return singleLogoutService;
    }

    @SuppressWarnings("unchecked")
    private <T> T build(QName elementName) {
        XMLObjectBuilder<?> builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(elementName);
        if (builder == null) {
            throw new Saml2Exception("Unable to resolve Builder for " + elementName);
        }
        return (T) builder.buildObject(elementName);
    }

    private String serialize(EntityDescriptor entityDescriptor) {
        try {
            Element element = this.entityDescriptorMarshaller.marshall(entityDescriptor);
            return SerializeSupport.prettyPrintXML(element);
        } catch (Exception ex) {
            throw new Saml2Exception(ex);
        }
    }

}
