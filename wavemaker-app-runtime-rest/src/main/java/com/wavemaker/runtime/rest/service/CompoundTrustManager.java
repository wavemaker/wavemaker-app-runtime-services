package com.wavemaker.runtime.rest.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class CompoundTrustManager implements X509TrustManager {
    private X509TrustManager applicationTrustManager;

    private X509TrustManager systemTrustManager;

    private X509Certificate[] acceptedIssuers;

    public CompoundTrustManager(X509TrustManager applicationTrustManager, X509TrustManager systemTrustManager) {
        this.applicationTrustManager = applicationTrustManager;
        this.systemTrustManager = systemTrustManager;
        this.acceptedIssuers = getMergedAcceptedIssuers();
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.acceptedIssuers;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        try {
            applicationTrustManager.checkClientTrusted(x509Certificates, s);
        } catch (CertificateException e) {
            systemTrustManager.checkClientTrusted(x509Certificates, s);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        try {
            applicationTrustManager.checkServerTrusted(x509Certificates, s);
        } catch (CertificateException e) {
            systemTrustManager.checkServerTrusted(x509Certificates, s);
        }
    }

    private X509Certificate[] getMergedAcceptedIssuers() {
        X509Certificate[] customAcceptedIssuers = applicationTrustManager.getAcceptedIssuers();
        X509Certificate[] systemAcceptedIssuers = systemTrustManager.getAcceptedIssuers();
        X509Certificate[] result = new X509Certificate[customAcceptedIssuers.length + systemAcceptedIssuers.length];
        System.arraycopy(customAcceptedIssuers, 0, result, 0, customAcceptedIssuers.length);
        System.arraycopy(systemAcceptedIssuers, 0, result, customAcceptedIssuers.length, systemAcceptedIssuers.length);
        return result;
    }
}
