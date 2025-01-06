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

import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.wavemaker.app.security.models.TrustStoreConfig.TrustStoreConfigType;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.proxy.AppPropertiesConstants;

/**
 * @author Uday Shankar
 */
public class HttpConfiguration {

    private static final Pattern TLS_VERSION_PATTERN = Pattern.compile("TLSv1\\.[23]");
    private boolean useSystemProperties;
    private int connectionSocketTimeoutInSeconds;
    private int connectionTimeoutInSeconds;
    private int connectionRequestTimeoutInSeconds;
    private int maxTotalConnections;
    private int maxTotalConnectionsPerRoute;
    private int connectionTimeToLive;
    private String[] tlsVersions;
    private boolean requestBodyBufferingEnabled;
    private boolean appProxyEnabled;
    private String appProxyHost;
    private int appProxyPort;
    private String appProxyUsername;
    private String appProxyPassword;
    private String appProxyIncludeUrls;
    private String appProxyExcludeUrls;
    private boolean mtlsEnabled;
    private String keyStoreFile;
    private String keyStoreFileType;
    private String keyStorePassword;
    private TrustStoreConfigType trustStoreConfigType;
    private String trustStoreFile;
    private String trustStoreFileType;
    private String trustStorePassword;
    private boolean hostNameVerificationEnabled;

    @Autowired
    public HttpConfiguration(Environment environment) {
        useSystemProperties = environment.getProperty("app.rest.useSystemProperties", Boolean.class, false);
        connectionSocketTimeoutInSeconds = environment.getProperty("app.rest.connectionSocketTimeout", Integer.class, 360);
        connectionTimeoutInSeconds = environment.getProperty("app.rest.connectionTimeout", Integer.class, 30);
        connectionRequestTimeoutInSeconds = environment.getProperty("app.rest.connectionRequestTimeout", Integer.class, 5);
        maxTotalConnections = environment.getProperty("app.rest.maxTotalConnections", Integer.class, 100);
        maxTotalConnectionsPerRoute = environment.getProperty("app.rest.maxConnectionsPerRoute", Integer.class, 50);
        connectionTimeToLive = environment.getProperty("app.rest.connectionTimeToLive", Integer.class, -1);
        tlsVersions = getTlsVersions(environment);
        requestBodyBufferingEnabled = environment.getProperty("app.rest.requestBodyBufferingEnabled", Boolean.class, false);

        appProxyEnabled = environment.getProperty(AppPropertiesConstants.APP_PROXY_ENABLED, Boolean.class, false);
        appProxyHost = environment.getProperty(AppPropertiesConstants.APP_PROXY_HOST);
        appProxyPort = environment.getProperty(AppPropertiesConstants.APP_PROXY_PORT, Integer.class, -1);
        appProxyUsername = environment.getProperty(AppPropertiesConstants.APP_PROXY_USERNAME, "");
        appProxyPassword = environment.getProperty(AppPropertiesConstants.APP_PROXY_PASSWORD, "");
        appProxyIncludeUrls = environment.getProperty(AppPropertiesConstants.APP_PROXY_INCLUDE_URLS, "");
        appProxyExcludeUrls = environment.getProperty(AppPropertiesConstants.APP_PROXY_EXCLUDE_URLS, "");
        mtlsEnabled = environment.getProperty("security.general.mtls.enabled", Boolean.class, false);
        keyStoreFile = environment.getProperty("security.general.mtls.keystore.file", "");
        keyStoreFileType = environment.getProperty("security.general.mtls.keystore.fileType", "");
        keyStorePassword = environment.getProperty("security.general.mtls.keystore.password", "");
        trustStoreConfigType = environment.getProperty("security.general.truststore.config", TrustStoreConfigType.class, TrustStoreConfigType.SYSTEM_ONLY);
        trustStoreFile = environment.getProperty("security.general.truststore.file", "");
        trustStoreFileType = environment.getProperty("security.general.truststore.fileType", "");
        trustStorePassword = environment.getProperty("security.general.truststore.password", "");
        hostNameVerificationEnabled = environment.getProperty("security.general.client.ssl.hostNameVerification.enabled", Boolean.class, true);
    }

    public boolean isUseSystemProperties() {
        return useSystemProperties;
    }

    public int getConnectionSocketTimeoutInSeconds() {
        return connectionSocketTimeoutInSeconds;
    }

    public int getConnectionTimeoutInSeconds() {
        return connectionTimeoutInSeconds;
    }

    public int getConnectionRequestTimeoutInSeconds() {
        return connectionRequestTimeoutInSeconds;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public int getMaxTotalConnectionsPerRoute() {
        return maxTotalConnectionsPerRoute;
    }

    public int getConnectionTimeToLive() {
        return connectionTimeToLive;
    }

    public String[] getTlsVersions() {
        return tlsVersions;
    }

    public boolean isRequestBodyBufferingEnabled() {
        return requestBodyBufferingEnabled;
    }

    public boolean isAppProxyEnabled() {
        return this.appProxyEnabled;
    }

    public String getAppProxyHost() {
        return this.appProxyHost;
    }

    public int getAppProxyPort() {
        return this.appProxyPort;
    }

    public String getAppProxyUsername() {
        return this.appProxyUsername;
    }

    public String getAppProxyPassword() {
        return this.appProxyPassword;
    }

    public String getAppProxyIncludeUrls() {
        return appProxyIncludeUrls;
    }

    public String getAppProxyExcludeUrls() {
        return appProxyExcludeUrls;
    }

    public boolean isMtlsEnabled() {
        return mtlsEnabled;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public String getKeyStoreFileType() {
        return keyStoreFileType;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public String getTrustStoreFileType() {
        return trustStoreFileType;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public TrustStoreConfigType getTrustStoreConfigType() {
        return trustStoreConfigType;
    }

    public boolean isHostNameVerificationEnabled() {
        return hostNameVerificationEnabled;
    }

    @Override
    public String toString() {
        return "HttpConfiguration{" +
            "useSystemProperties=" + useSystemProperties +
            ", connectionSocketTimeoutInSeconds=" + connectionSocketTimeoutInSeconds +
            ", connectionTimeoutInSeconds=" + connectionTimeoutInSeconds +
            ", connectionRequestTimeoutInSeconds=" + connectionRequestTimeoutInSeconds +
            ", maxTotalConnections=" + maxTotalConnections +
            ", maxTotalConnectionsPerRoute=" + maxTotalConnectionsPerRoute +
            ", connectionTimeToLive=" + connectionTimeToLive +
            ", tlsVersions=" + Arrays.toString(tlsVersions) +
            ", appProxyEnabled=" + appProxyEnabled +
            ", appProxyHost='" + appProxyHost + '\'' +
            ", appProxyPort=" + appProxyPort +
            ", appProxyUsername='" + appProxyUsername + '\'' +
            ", appProxyIncludeUrls='" + appProxyIncludeUrls + '\'' +
            ", appProxyExcludeUrls='" + appProxyExcludeUrls + '\'' +
            ", mtlsEnabled=" + mtlsEnabled +
            ", keyStoreFile='" + keyStoreFile + '\'' +
            ", keyStoreFileType='" + keyStoreFileType + '\'' +
            ", trustStoreConfigType=" + trustStoreConfigType +
            ", trustStoreFile='" + trustStoreFile + '\'' +
            ", trustStoreFileType='" + trustStoreFileType + '\'' +
            ", hostNameVerificationEnabled=" + hostNameVerificationEnabled +
            '}';
    }

    private String[] getTlsVersions(Environment environment) {
        String[] tlsVersionsArray = environment.getProperty("app.rest.tlsVersions", String.class, "TLSv1.3,TLSv1.2").split(",");
        boolean isValidTlsVersions = Arrays.stream(tlsVersionsArray).allMatch(TLS_VERSION_PATTERN.asMatchPredicate());
        if (!isValidTlsVersions) {
            throw new WMRuntimeException("Invalid value configured in app.rest.tlsVersions=" + Arrays.toString(tlsVersionsArray));
        }
        return tlsVersionsArray;
    }
}
