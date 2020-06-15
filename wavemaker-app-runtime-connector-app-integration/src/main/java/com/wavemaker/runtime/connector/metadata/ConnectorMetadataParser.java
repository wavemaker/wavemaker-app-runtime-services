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
