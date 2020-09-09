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
package com.wavemaker.runtime.connector.context;

import com.wavemaker.runtime.connector.metadata.ConnectorMetadata;

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
