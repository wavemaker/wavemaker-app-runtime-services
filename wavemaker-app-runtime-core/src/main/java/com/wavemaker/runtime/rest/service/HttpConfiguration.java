package com.wavemaker.runtime.rest.service;

import org.springframework.core.env.Environment;

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

    public HttpConfiguration(Environment environment) {
        useSystemProperties = environment.getProperty("app.rest.useSystemProperties", Boolean.class, false);
        connectionSocketTimeoutInSeconds = environment.getProperty("app.rest.connectionSocketTimeout", Integer.class, 360);
        connectionTimeoutInSeconds = environment.getProperty("app.rest.connectionTimeout", Integer.class, 30);
        connectionRequestTimeoutInSeconds = environment.getProperty("app.rest.connectionRequestTimeout", Integer.class, 5);
        maxTotalConnections = environment.getProperty("app.rest.maxTotalConnections", Integer.class, 100);
        maxTotalConnectionsPerRoute = environment.getProperty("app.rest.maxConnectionsPerRoute", Integer.class, 50);
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

    @Override
    public String toString() {
        return "HttpConfiguration{" +
                "useSystemProperties=" + useSystemProperties +
                ", connectionSocketTimeoutInSeconds=" + connectionSocketTimeoutInSeconds +
                ", connectionTimeoutInSeconds=" + connectionTimeoutInSeconds +
                ", connectionRequestTimeoutInSeconds=" + connectionRequestTimeoutInSeconds +
                ", maxTotalConnections=" + maxTotalConnections +
                ", maxTotalConnectionsPerRoute=" + maxTotalConnectionsPerRoute +
                '}';
    }
}
