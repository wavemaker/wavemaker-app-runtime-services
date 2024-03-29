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
package com.wavemaker.runtime.connector.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.wavemaker.commons.util.WMIOUtils;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 30/5/20
 */
public class ConnectorMetadataParser {

    public static ConnectorMetadata parser(URL resource) {
        try {
            return parser(resource.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse connector YAML file", e);
        }

    }

    public static ConnectorMetadata parser(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(ConnectorMetadata.class, new LoaderOptions()));
        try {
            return yaml.loadAs(is, ConnectorMetadata.class);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to parse connector YAML file", e);
        } finally {
            WMIOUtils.closeSilently(is);
        }
    }
}
