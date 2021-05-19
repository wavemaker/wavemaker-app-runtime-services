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
package com.wavemaker.runtime.security.provider.saml;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.WMIOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.X509Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.saml.metadata.MetadataManager;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

/**
 * Created by ArjunSahasranam on 28/11/16.
 */
public class LoadKeyStore {

    private static final Logger logger = LoggerFactory.getLogger(LoadKeyStore.class);

    private static final String PROVIDERS_SAML_KEY_STORE_FILE = "providers.saml.keyStoreFile";
    private static final String PROVIDERS_SAML_KEY_STORE_PASSWORD = "providers.saml.keyStorePassword";
    private static final String KEY = "idpkey";

    private Environment environment;
    private MetadataManager metadataManager;

    public LoadKeyStore(Environment environment, MetadataManager metadataManager) {
        this.environment = environment;
        this.metadataManager = metadataManager;
    }

    public void load() {
        final String keyStoreFileName = environment.getProperty(PROVIDERS_SAML_KEY_STORE_FILE);
        final String keyStorePassword = environment.getProperty(PROVIDERS_SAML_KEY_STORE_PASSWORD);

        if (StringUtils.isNotBlank(keyStoreFileName) && StringUtils.isNotBlank(keyStorePassword)) {
            File keyStoreFile = new File(getFileURI("saml/" + keyStoreFileName));
            InputStream resourceAsStream;
            try {
                resourceAsStream = new FileInputStream(keyStoreFile);
            } catch (FileNotFoundException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.file.not.found"), e, keyStoreFileName);
            }
            final KeyStore keyStore = load(resourceAsStream, keyStorePassword);
            String idpPublicKey;
            try {
                XMLObject xmlObject = metadataManager.getAvailableProviders().iterator().next().getMetadata();
                idpPublicKey = readIdpPublicKey(xmlObject);
            } catch (MetadataProviderException e) {
                throw new WMRuntimeException(e);
            }
            final boolean success = importCertificate(keyStore, KEY, idpPublicKey);
            if (success) {
                saveKeyStore(keyStore, keyStoreFile, keyStorePassword);
            } else {
                logger.info("Certificate with {} already found", KEY);
            }
        } else {
            logger.info("saml properties not found or configured");
        }
    }

    private KeyStore load(InputStream keyStoreIS, String password) {
        try {
            logger.info("loading keystore");
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(keyStoreIS, password.toCharArray());
            return keystore;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.error.creating.keystore"), e);
        } finally {
            WMIOUtils.closeSilently(keyStoreIS);
        }
    }

    private String readIdpPublicKey(XMLObject xmlObject) {
        final IDPSSODescriptor idpssoDescriptor = ((EntityDescriptor) xmlObject).getIDPSSODescriptor(SAMLConstants.SAML_2_0_PROTOCOL);
        final List<KeyDescriptor> keyDescriptors = idpssoDescriptor.getKeyDescriptors();
        logger.info("Size of the keyDescriptors is : {}", keyDescriptors.size());
        for (KeyDescriptor keyDescriptor : keyDescriptors) {
            UsageType usageType = keyDescriptor.getUse();
            logger.info("KeyDescriptor usage type is {}", usageType);
            if (UsageType.SIGNING == usageType) {
                final KeyInfo keyInfo = keyDescriptor.getKeyInfo();
                final X509Data x509Data = keyInfo.getX509Datas().get(0);
                final org.opensaml.xml.signature.X509Certificate x509Certificate = x509Data.getX509Certificates()
                        .get(0);
                return com.wavemaker.commons.util.StringUtils.removeLineFeed(x509Certificate.getValue());
            }
        }
        throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.publicKey.not.found"));
    }

    private String createIdpCertificate(final String idpPublicKey) {
        logger.info("create certificate for {}", idpPublicKey);
        StringBuilder idpCertificateBuilder = new StringBuilder();
        idpCertificateBuilder.append("-----BEGIN CERTIFICATE-----\n");
        idpCertificateBuilder.append(idpPublicKey + "\n");
        idpCertificateBuilder.append("-----END CERTIFICATE-----\n");
        return idpCertificateBuilder.toString();
    }

    private boolean importCertificate(KeyStore keystore, String keyAlias, String idpPublicKey) {
        logger.info("import certificate for {} with key {}", idpPublicKey, keyAlias);
        InputStream certIn = null;
        try {
            if (keystore.containsAlias(keyAlias)) {
                return false;
            }
            String idpPublicCertificate = createIdpCertificate(idpPublicKey);
            certIn = new ByteArrayInputStream(idpPublicCertificate.getBytes());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (certIn.available() > 0) {
                Certificate cert = cf.generateCertificate(certIn);
                keystore.setCertificateEntry(keyAlias, cert);
            }
        } catch (CertificateException | KeyStoreException | IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.certificate.import.error"), e);
        } finally {
            WMIOUtils.closeSilently(certIn);
        }
        return true;
    }

    private void saveKeyStore(KeyStore keyStore, File keyStoreFile, String password) {
        logger.info("save keystore");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(keyStoreFile);
            keyStore.store(stream, password.toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.error.saving.keystore"), e);
        } finally {
            WMIOUtils.closeSilently(stream);
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
