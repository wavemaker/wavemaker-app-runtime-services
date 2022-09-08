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
package com.wavemaker.runtime.connector.cache;

import java.util.Objects;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 3/6/20
 */
public class ConnectorConfiguration {

    private String connectorId;

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
