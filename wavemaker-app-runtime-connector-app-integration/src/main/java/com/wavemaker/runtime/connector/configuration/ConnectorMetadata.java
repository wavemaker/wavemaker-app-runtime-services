package com.wavemaker.runtime.connector.configuration;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 30/5/20
 */
public class ConnectorMetadata {

    private String version;
    private String name;
    private String description;
    private String configurationclass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfigurationclass() {
        return configurationclass;
    }

    public void setConfigurationclass(String configurationclass) {
        this.configurationclass = configurationclass;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
