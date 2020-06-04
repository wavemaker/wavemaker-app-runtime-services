package com.wavemaker.runtime.connector.cache;

import java.util.Objects;

import org.springframework.lang.NonNull;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 3/6/20
 */
public class ConnectorConfiguration {

    @NonNull
    private String connectorId;

    @NonNull
    private String configurationId;

    public ConnectorConfiguration(String connectorId, String configurationId) {
        this.connectorId = connectorId;
        this.configurationId = configurationId;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectorConfiguration that = (ConnectorConfiguration) o;
        return connectorId.equals(that.connectorId) &&
                configurationId.equals(that.configurationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorId, configurationId);
    }
}
