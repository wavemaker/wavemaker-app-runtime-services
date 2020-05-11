package com.wavemaker.runtime.connector.classloader;

import com.wavemaker.runtime.connector.exception.ConnectorDoesNotExist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 26/3/20
 */
public class WMConnectorClassLoaderProvider {

    private static final Logger logger = LoggerFactory.getLogger(WMConnectorClassLoaderProvider.class);

    private String connectorPath = "/WEB-INF/connectors/${0}/impl/";

    @Autowired
    private ServletContext context;

    public ClassLoader getClassLoader(String connectorId, ClassLoader appClassLoader) {
        logger.info("Building impl classloader for connector {0}", connectorId);
        String dependenciesPath = connectorPath.replace("${0}", connectorId.toLowerCase());
        URL url;
        try {
            url = context.getResource(dependenciesPath);
            if (url == null) {
                throw new ConnectorDoesNotExist("Connector " + connectorId + "does not exist");
            }
        } catch (MalformedURLException e) {
            throw new ConnectorDoesNotExist("Connector {0} does not exist", e);
        }

        return buildClassLoader(url,appClassLoader);
    }

    private ClassLoader buildClassLoader(URL url, ClassLoader appClassLoader) {
        try {
            String path = url.getPath();
            File[] fList = new File(path).listFiles();
            URL[] urls = new URL[fList.length];
            int i = 0;
            for (File file : fList) {
                urls[i] = file.toURI().toURL();
                i++;
            }
            return new ConnectorImplFirstClassLoader(urls,appClassLoader);
        } catch (MalformedURLException e) {
            throw new ConnectorDoesNotExist("Failed to build url class loader from connector ", e);
        }
    }

}
