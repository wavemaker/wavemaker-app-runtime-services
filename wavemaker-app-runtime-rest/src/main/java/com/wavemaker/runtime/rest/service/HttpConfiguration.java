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

import org.springframework.core.env.Environment;

import com.wavemaker.commons.proxy.AppPropertiesConstants;
import com.wavemaker.runtime.rest.TrustStoreConfig;

/**
 * @author Uday Shankar
 */
public class HttpConfiguration {
    private boolean useSystemProperties;
    private int connectionSocketTimeoutInSeconds;
    private int connectionTimeoutInSeconds;
    private int connectionRequestTimeoutInSeconds;
    private int maxTotalConnections;
    private int maxTotalConnectionsPerRoute;

    private boolean appProxyEnabled;
    private String appProxyHost;
    private int appProxyPort;
    private String appProxyUsername;
    private String appProxyPassword;
    private boolean mtlsEnabled;
    private String keyStoreFile;
    private String keyStoreFileType;
    private String keyStorePassword;
    private TrustStoreConfig trustStoreConfig;
    private String trustStoreFile;
    private String trustStoreFileType;
    private String trustStorePassword;
    private boolean hostNameVerificationEnabled;

    public HttpConfiguration(Environment environment) {
        useSystemProperties = environment.getProperty("app.rest.useSystemProperties", Boolean.class, false);
        connectionSocketTimeoutInSeconds = environment.getProperty("app.rest.connectionSocketTimeout", Integer.class, 360);
        connectionTimeoutInSeconds = environment.getProperty("app.rest.connectionTimeout", Integer.class, 30);
        connectionRequestTimeoutInSeconds = environment.getProperty("app.rest.connectionRequestTimeout", Integer.class, 5);
        maxTotalConnections = environment.getProperty("app.rest.maxTotalConnections", Integer.class, 100);
        maxTotalConnectionsPerRoute = environment.getProperty("app.rest.maxConnectionsPerRoute", Integer.class, 50);

        appProxyEnabled = environment.getProperty(AppPropertiesConstants.APP_PROXY_ENABLED, Boolean.class, false);
        appProxyHost = environment.getProperty(AppPropertiesConstants.APP_PROXY_HOST);
        appProxyPort = environment.getProperty(AppPropertiesConstants.APP_PROXY_PORT, Integer.class, -1);
        appProxyUsername = environment.getProperty(AppPropertiesConstants.APP_PROXY_USERNAME, "");
        appProxyPassword = environment.getProperty(AppPropertiesConstants.APP_PROXY_PASSWORD, "");
        mtlsEnabled = environment.getProperty("security.general.mtls.enabled", Boolean.class, false);
        keyStoreFile = environment.getProperty("security.general.mtls.keystore.file", "");
        keyStoreFileType = environment.getProperty("security.general.mtls.keystore.fileType", "");
        keyStorePassword = environment.getProperty("security.general.mtls.keystore.password", "");
        trustStoreConfig = environment.getProperty("security.general.truststore.config", TrustStoreConfig.class, TrustStoreConfig.SYSTEM_ONLY);
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

    public TrustStoreConfig getTrustStoreConfig() {
        return trustStoreConfig;
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
            ", appProxyEnabled=" + appProxyEnabled +
            ", appProxyHost='" + appProxyHost + '\'' +
            ", appProxyPort=" + appProxyPort +
            ", appProxyUsername='" + appProxyUsername + '\'' +
            ", mtlsEnabled=" + mtlsEnabled +
            ", keyStoreFile='" + keyStoreFile + '\'' +
            ", keyStoreFileType='" + keyStoreFileType + '\'' +
            ", trustStoreConfig=" + trustStoreConfig +
            ", trustStoreFile='" + trustStoreFile + '\'' +
            ", trustStoreFileType='" + trustStoreFileType + '\'' +
            ", hostNameVerificationEnabled=" + hostNameVerificationEnabled +
            '}';
    }
}
