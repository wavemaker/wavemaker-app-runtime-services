/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
