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
package com.wavemaker.runtime.connector.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 30/5/20
 */
public class ConnectorMetadataParser {

    public static ConnectorMetadata parser(URL resource) {
        try {
            return parser(resource.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse connector YAML file ");
        }

    }

    public static ConnectorMetadata parser(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(ConnectorMetadata.class));
        try {
            return yaml.load(is);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to parse connector YAML file ");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to close stream", e);
                }
            }
        }
    }
}
