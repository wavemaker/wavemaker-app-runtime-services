package com.wavemaker.runtime.connector.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.runtime.connector.classloader.ConnectorContextResourceProvider;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 4/6/20
 */
public class ConnectorContextBuilder {

    @Autowired
    private ServletContext context;

    private ConnectorContextResourceProvider contextResourceProvider;
    private Map<String, ConnectorContext> connectorContextMap = new ConcurrentHashMap<>();

    public ConnectorContext build(String connectorId, ClassLoader appClassLoader) {
        contextResourceProvider = contextResourceProvider == null ? new ConnectorContextResourceProvider(context) : contextResourceProvider;
        if (connectorContextMap.get(connectorId) == null) {
            ConnectorContext context = new ConnectorContext(contextResourceProvider.getClassLoader(connectorId, appClassLoader), contextResourceProvider.getConnectorMetadata(connectorId));
            connectorContextMap.put(connectorId, context);

        }
        return connectorContextMap.get(connectorId);
    }

}
