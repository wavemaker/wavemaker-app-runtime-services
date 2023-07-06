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

package com.wavemaker.runtime.rest.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.app.security.models.TrustStoreConfig.TrustStoreConfigType;
import com.wavemaker.commons.util.SSLUtils;

public class SSLContextProvider {

    private static final Logger logger = LoggerFactory.getLogger(SSLContextProvider.class);

    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;
    private HttpConfiguration httpConfiguration;

    @Autowired
    public SSLContextProvider(HttpConfiguration httpConfiguration) {
        this.httpConfiguration = httpConfiguration;
        initSslContext();
        initHostnameVerifier();
    }

    private void initSslContext() {
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(getKeyManager(), getTrustManager(), new SecureRandom());
        } catch (Exception e) {
            logger.warn("Failed in initialize ssl context", e);
            throw new WMRuntimeException(e);
        }
    }

    private void initHostnameVerifier() {
        if (httpConfiguration.isHostNameVerificationEnabled()) {
            logger.info("Initializing default host name verifier");
            hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        } else {
            logger.info("Initializing Noop host name verifier");
            hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        }
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    private KeyManager[] getKeyManager() {
        if (httpConfiguration.isMtlsEnabled()) {
            try {
                logger.info("Loading private key:{} to keystore", httpConfiguration.getKeyStoreFile());
                KeyStore keyStore = getKeyStore(httpConfiguration.getKeyStoreFileType(), httpConfiguration.getKeyStoreFile(), httpConfiguration.getKeyStorePassword());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, httpConfiguration.getKeyStorePassword().toCharArray());
                return keyManagerFactory.getKeyManagers();
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
                throw new WMRuntimeException(e);
            }
        }
        return null;
    }

    private TrustManager[] getTrustManager() {
        TrustStoreConfigType trustStoreConfigType = httpConfiguration.getTrustStoreConfigType();
        X509TrustManager x509TrustManager;
        logger.info("Using trust manager with {} Config", trustStoreConfigType);
        if (trustStoreConfigType.equals(TrustStoreConfigType.APPLICATION_ONLY)) {
            x509TrustManager = getApplicationTrustManager();
        } else if (trustStoreConfigType.equals(TrustStoreConfigType.SYSTEM_ONLY)) {
            x509TrustManager = getSystemTrustManager();
        } else if (trustStoreConfigType.equals(TrustStoreConfigType.APPLICATION_AND_SYSTEM)) {
            X509TrustManager applicationTrustManager = getApplicationTrustManager();
            X509TrustManager systemTrustManager = getSystemTrustManager();
            x509TrustManager = new CompoundTrustManager(List.of(applicationTrustManager, systemTrustManager));
        } else {
            x509TrustManager = SSLUtils.NoCheckTrustManager.INSTANCE;
        }
        return new TrustManager[]{x509TrustManager};
    }

    private X509TrustManager getApplicationTrustManager() {
        logger.info("Loading application trust manager:{} to truststore", httpConfiguration.getTrustStoreFile());
        KeyStore trustStore = getKeyStore(httpConfiguration.getTrustStoreFileType(), httpConfiguration.getTrustStoreFile(), httpConfiguration.getTrustStorePassword());
        return getX509TrustManager(trustStore);
    }

    private X509TrustManager getSystemTrustManager() {
        logger.info("Loading system trust manager to truststore");
        return getX509TrustManager(null);
    }

    private KeyStore getKeyStore(String keyStoreFileType, String keyStoreFile, String keyStorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreFileType);
            UrlResource resource = new UrlResource(keyStoreFile);
            if (!resource.exists()) {
                throw new WMRuntimeException(keyStoreFile + " not found");
            }
            keyStore.load(resource.getInputStream(), keyStorePassword.toCharArray());
            return keyStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new WMRuntimeException(e);
        }
    }

    private X509TrustManager getX509TrustManager(KeyStore trustStore) {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    return (X509TrustManager) trustManager;
                }
            }
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new WMRuntimeException(e);
        }
        throw new IllegalStateException();
    }

}
