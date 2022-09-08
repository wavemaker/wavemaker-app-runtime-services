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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import com.wavemaker.commons.WMRuntimeException;

public class CompoundTrustManager implements X509TrustManager {

    private List<X509TrustManager> trustManagerList;

    private X509Certificate[] acceptedIssuers;

    public CompoundTrustManager(List<X509TrustManager> trustManagerList) {
        this.trustManagerList = trustManagerList;
        this.acceptedIssuers = getMergedAcceptedIssuers();
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.acceptedIssuers;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        for (X509TrustManager trustManager : trustManagerList) {
            if (checkIsClientTrusted(trustManager, x509Certificates, s)) {
                return;
            }
        }
        throw new WMRuntimeException("The TrustStore does contain the certificate");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        for (X509TrustManager trustManager : trustManagerList) {
            if (checkIsServerTrusted(trustManager, x509Certificates, s)) {
                return;
            }
        }
        throw new WMRuntimeException("The TrustStore does contain the certificate");
    }

    private boolean checkIsClientTrusted(X509TrustManager trustManager, X509Certificate[] x509Certificates, String s) {
        try {
            trustManager.checkClientTrusted(x509Certificates, s);
            return true;
        } catch (CertificateException e) {
            return false;
        }
    }

    private boolean checkIsServerTrusted(X509TrustManager trustManager, X509Certificate[] x509Certificates, String s) {
        try {
            trustManager.checkServerTrusted(x509Certificates, s);
            return true;
        } catch (CertificateException e) {
            return false;
        }
    }

    private X509Certificate[] getMergedAcceptedIssuers() {
        List<X509Certificate> mergedAcceptedIssuers = new ArrayList<>();
        for (X509TrustManager trustManager : trustManagerList) {
            mergedAcceptedIssuers.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
        }
        return mergedAcceptedIssuers.toArray(X509Certificate[]::new);
    }
}
