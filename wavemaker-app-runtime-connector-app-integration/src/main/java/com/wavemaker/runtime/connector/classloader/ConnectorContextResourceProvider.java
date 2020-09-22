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
package com.wavemaker.runtime.connector.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wavemaker.runtime.connector.exception.ConnectorDoesNotExist;
import com.wavemaker.runtime.connector.metadata.ConnectorMetadata;
import com.wavemaker.runtime.connector.metadata.ConnectorMetadataParser;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 26/3/20
 */
public class ConnectorContextResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorContextResourceProvider.class);

    private String connectorImplPath = "/WEB-INF/connectors/${0}/impl/";

    private String metadataFilePath = "/WEB-INF/connectors/${0}/config/connector-metadata.yaml";

    private ServletContext context;

    public ConnectorContextResourceProvider(ServletContext context) {
        this.context = context;
    }

    public ClassLoader getClassLoader(String connectorId, ClassLoader appClassLoader) {
        logger.info("Building impl classloader for connector {}", connectorId);
        String dependenciesPath = connectorImplPath.replace("${0}", connectorId);
        URL url;
        try {
            url = context.getResource(dependenciesPath);
            if (url == null) {
                throw new ConnectorDoesNotExist("Connector " + connectorId + "does not exist");
            }
        } catch (MalformedURLException e) {
            logger.error("Connector {} directory does not exist ", connectorId);
            throw new ConnectorDoesNotExist("Connector {0} does not exist", e);
        }
        return buildClassLoader(connectorId, url, appClassLoader);
    }

    public ConnectorMetadata getConnectorMetadata(String connectorId) {
        String resolvedPath = metadataFilePath.replace("${0}", connectorId);
        try {
            URL url = context.getResource(resolvedPath);
            return ConnectorMetadataParser.parser(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Connector metadata yaml file does not exist " + resolvedPath, e);
        }
    }

    private ClassLoader buildClassLoader(String connectorId, URL url, ClassLoader appClassLoader) {
        try {
            String path = url.getPath();
            File[] fList = new File(path).listFiles();
            URL[] urls = new URL[fList.length];
            int i = 0;
            for (File file : fList) {
                urls[i] = file.toURI().toURL();
                i++;
            }
            return new ConnectorImplFirstClassLoader(urls, appClassLoader);
        } catch (MalformedURLException e) {
            logger.error("Failed to build impl class cloader for connector {} ", connectorId);
            throw new ConnectorDoesNotExist("Failed to build impl class loader from connector " + connectorId, e);
        }
    }

}
