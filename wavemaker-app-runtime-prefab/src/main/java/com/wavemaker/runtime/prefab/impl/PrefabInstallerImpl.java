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
package com.wavemaker.runtime.prefab.impl;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import com.wavemaker.commons.classloader.WMUrlClassLoader;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.prefab.context.PrefabWebApplicationContext;
import com.wavemaker.runtime.prefab.core.Prefab;
import com.wavemaker.runtime.prefab.core.PrefabInstaller;
import com.wavemaker.runtime.prefab.core.PrefabManager;
import com.wavemaker.runtime.prefab.core.PrefabRegistry;

/**
 * Default implementation of {@link PrefabInstaller}. Hooks to the {@link ApplicationContext} of
 * the prefab servlet for installing/uninstalling the beans of the {@link Prefab}.
 * <p/>
 * Uses Spring event handling to communicate prefab state changes.
 *
 * @author Dilip Kumar
 */
@Service
public class PrefabInstallerImpl implements PrefabInstaller, ApplicationContextAware, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(PrefabInstallerImpl.class);

    @Override
    public void installPrefab(final Prefab prefab) {
        if (prefabRegistry == null) {
            return;
        }
        ConfigurableApplicationContext prefabContext = new PrefabWebApplicationContext(prefab, context, servletContext);
        prefabRegistry.addPrefabContext(prefab.getName(), prefabContext);
        prefab.setInstalled(true);
    }

    @Autowired
    private PrefabManager prefabManager;

    @Autowired
    private PrefabRegistry prefabRegistry;
    private ServletContext servletContext;

    private ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Sets the {@link PrefabManager} for reading the prefabs.
     */
    public void setPrefabManager(final PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

    /**
     * Sets the {@link PrefabRegistry}
     */
    public void setPrefabRegistry(final PrefabRegistry prefabRegistry) {
        this.prefabRegistry = prefabRegistry;
    }

    /**
     * @return the prefabManager
     */
    public PrefabManager getPrefabManager() {
        return prefabManager;
    }

    /**
     * @return the prefabRegistry
     */
    public PrefabRegistry getPrefabRegistry() {
        return prefabRegistry;
    }

    /**
     * (Re)installs all available prefabs.
     */
    public void installPrefabs() {
        if (prefabManager == null) {
            return;
        }

        for (Prefab prefab : prefabManager.getPrefabs()) {
            installPrefab(prefab);
        }
    }

    /**
     * Uninstalls all prefabs.
     */
    public void uninstallPrefabs() {
        if (prefabRegistry != null) {
            // closing application contexts
            prefabManager.getPrefabs().stream().filter(Prefab::isInstalled).forEach(prefab -> {
                ConfigurableApplicationContext prefabContext = prefabRegistry.getPrefabContext(prefab.getName());
                WMIOUtils.closeByLogging(prefabContext);
                prefab.setInstalled(false);
            });
            prefabRegistry.deletePrefabContexts();
        }

        if (prefabManager != null) {
            // closing class loader
            for (Prefab prefab : prefabManager.getPrefabs()) {
                WMUrlClassLoader wmUrlClassLoader = (WMUrlClassLoader) prefab.getClassLoader();
                WMIOUtils.closeByLogging(wmUrlClassLoader);
                prefab.setClassLoader(null);
            }
            prefabManager.deleteAllPrefabs();
        }
    }
}
