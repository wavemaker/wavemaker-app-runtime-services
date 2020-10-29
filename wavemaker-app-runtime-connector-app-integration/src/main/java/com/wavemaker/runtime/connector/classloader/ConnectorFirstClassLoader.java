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

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 3/5/20
 */
public class ConnectorFirstClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorFirstClassLoader.class);
    private final ClassLoader sysClzLoader;

    public ConnectorFirstClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        sysClzLoader = getSystemClassLoader();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(name)) {
            // has the class loaded already?
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                try {
                    if (sysClzLoader != null) {
                        loadedClass = sysClzLoader.loadClass(name);
                    }
                } catch (ClassNotFoundException ex) {
                    // class not found in system class loader... silently skipping
                }

                try {
                    // find the class from given jar urls as in first constructor parameter.
                    if (loadedClass == null) {
                        loadedClass = findClass(name);
                    }
                } catch (ClassNotFoundException e) {
                    // class is not found in the given urls.
                    // Let's try it in parent classloader.
                    // If class is still not found, then this method will throw class not found ex.
                    loadedClass = super.loadClass(name, resolve);
                }


            }

            if (resolve) {      // marked to resolve
                resolveClass(loadedClass);
            }
            return loadedClass;
        }
    }
}
