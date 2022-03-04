package com.wavemaker.runtime.security.provider.saml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.model.security.saml.MetadataSource;

@Configuration
public class WMSAMLSecurityConfig {

    @Autowired
    private Environment environment;
    private static final Logger logger = LoggerFactory.getLogger(WMSAMLSecurityConfig.class);
    private static final String PROVIDERS_SAML_KEY_STORE_FILE = "security.providers.saml.keyStoreFile";
    private static final String PROVIDERS_SAML_KEY_STORE_PASSWORD = "security.providers.saml.keyStorePassword";
    private static final String PROVIDERS_SAML_KEY_STORE_ALIAS = "security.providers.saml.keyAlias";
    private static final String PROVIDERS_SAML_IDP_METADATA_SOURCE = "security.providers.saml.idpMetadataSource";
    private static final String PROVIDERS_SAML_IDP_METADATA_FILE = "security.providers.saml.idpMetadataFile";
    private static final String PROVIDERS_SAML_IDP_METADATA_URL = "security.providers.saml.idpMetadataUrl";

    @Bean("relyingPartyRegistrations")
    public RelyingPartyRegistrationRepository relyingPartyRegistrations() {

        final String keyStoreFileName = environment.getProperty(PROVIDERS_SAML_KEY_STORE_FILE);
        final String keyStorePassword = environment.getProperty(PROVIDERS_SAML_KEY_STORE_PASSWORD);
        final String keyStoreAlias = environment.getProperty(PROVIDERS_SAML_KEY_STORE_ALIAS);
        final String idpMetadataSource = environment.getProperty(PROVIDERS_SAML_IDP_METADATA_SOURCE);
        final String idpMetadataUrl = environment.getProperty(PROVIDERS_SAML_IDP_METADATA_URL);
        final String idpMetadataFile = environment.getProperty(PROVIDERS_SAML_IDP_METADATA_FILE);

        if (StringUtils.isNotBlank(keyStoreFileName) && StringUtils.isNotBlank(keyStorePassword) && StringUtils.isNotBlank(keyStoreAlias) && StringUtils.isNotBlank(idpMetadataSource)) {
            File keyStoreFile = new File(getFileURI("saml/" + keyStoreFileName));
            InputStream resourceAsStream;
            try {
                resourceAsStream = new FileInputStream(keyStoreFile);
            } catch (FileNotFoundException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.file.not.found"), e, keyStoreFileName);
            }
            PrivateKey privateKey;
            X509Certificate cert;
            try {
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(resourceAsStream, keyStorePassword.toCharArray());
                privateKey = (PrivateKey) keystore.getKey(keyStoreAlias, keyStorePassword.toCharArray());
                cert = (X509Certificate) keystore.getCertificate(keyStoreAlias);
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | IOException e) {
                throw new WMRuntimeException("Error in reading private key and certificate from keystore file ", e);
            }
            RelyingPartyRegistration.Builder relyingPartyBuilder;
            if (idpMetadataSource.equals(MetadataSource.URL.name())) {
                relyingPartyBuilder = RelyingPartyRegistrations.fromMetadataLocation(idpMetadataUrl);
            } else {
                relyingPartyBuilder = RelyingPartyRegistrations.fromMetadataLocation(idpMetadataFile);
            }

            RelyingPartyRegistration registration = relyingPartyBuilder
                    .registrationId("saml")
                    .signingX509Credentials((c) -> c.add(Saml2X509Credential.signing(privateKey, cert)))
                    .decryptionX509Credentials((c) -> c.add(Saml2X509Credential.decryption(privateKey, cert)))
                    .singleLogoutServiceLocation("{baseUrl}/logout/saml2/slo")
                    .build();
            return new InMemoryRelyingPartyRegistrationRepository(registration);
        } else {
            throw new WMRuntimeException("saml properties not found or configured");
        }
    }

    private URI getFileURI(String filePath) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final URL resource = contextClassLoader.getResource(filePath);
        try {
            return resource.toURI();
        } catch (URISyntaxException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.file.not.found"), e, filePath);
        }
    }

}
