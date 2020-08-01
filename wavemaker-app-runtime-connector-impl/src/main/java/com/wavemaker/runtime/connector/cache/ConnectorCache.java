package com.wavemaker.runtime.connector.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 3/6/20
 */
public class ConnectorCache {

    private static Map<ConnectorConfiguration, ApplicationContext> applicationContextMap = new ConcurrentHashMap<>();

    public static ApplicationContext get(ConnectorConfiguration key) {
        return applicationContextMap.get(key);
    }

    public static void put(ConnectorConfiguration key, ApplicationContext context) {
        applicationContextMap.put(key, context);
    }

    public static void delete(ConnectorConfiguration key) {
        applicationContextMap.remove(key);
    }

    public static void deleteAll() {
        applicationContextMap.clear();
    }
}
