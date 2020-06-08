package com.wavemaker.runtime.connector.configuration;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 30/5/20
 */
public class ConnectorMetadata {

    private String version;
    private String name;
    private String description;
    private String springConfigurationClass;

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

    public String getSpringConfigurationClass() {
        return springConfigurationClass;
    }

    public void setSpringConfigurationClass(String springConfigurationClass) {
        this.springConfigurationClass = springConfigurationClass;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
