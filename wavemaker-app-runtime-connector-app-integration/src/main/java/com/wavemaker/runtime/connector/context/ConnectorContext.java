package com.wavemaker.runtime.connector.context;

import com.wavemaker.runtime.connector.configuration.ConnectorMetadata;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 4/6/20
 */
public class ConnectorContext {

    private ConnectorMetadata connectorMetadata;
    private ClassLoader classLoader;

    public ConnectorContext(ClassLoader classLoader, ConnectorMetadata connectorMetadata) {
        this.connectorMetadata = connectorMetadata;
        this.classLoader = classLoader;
    }

    public ConnectorMetadata getConnectorMetadata() {
        return connectorMetadata;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
