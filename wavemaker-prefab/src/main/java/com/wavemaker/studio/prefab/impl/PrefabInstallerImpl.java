/**
 * Copyright (C) 2014 WaveMaker, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.studio.prefab.impl;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import com.wavemaker.common.classloader.PrefabClassLoader;
import com.wavemaker.studio.prefab.context.PrefabWebApplicationContext;
import com.wavemaker.studio.prefab.core.Prefab;
import com.wavemaker.studio.prefab.core.PrefabInstaller;
import com.wavemaker.studio.prefab.core.PrefabManager;
import com.wavemaker.studio.prefab.core.PrefabRegistry;
import com.wavemaker.studio.prefab.event.PrefabEvent;
import com.wavemaker.studio.prefab.event.PrefabsLoadedEvent;
import com.wavemaker.studio.prefab.event.PrefabsUnloadedEvent;

/**
 * Default implementation of {@link PrefabInstaller}. Hooks to the {@link ApplicationContext} of
 * the prefab servlet for installing/uninstalling the beans of the {@link Prefab}.
 * <p/>
 * Uses Spring event handling to communicate prefab state changes.
 *
 * @author Dilip Kumar
 */
@Service
public class PrefabInstallerImpl implements PrefabInstaller, ApplicationContextAware,
        ApplicationListener<PrefabEvent>, ServletContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrefabInstallerImpl.class);

    @Override
    public void installPrefab(final Prefab prefab) {
        if (prefabRegistry == null) {
            return;
        }
        ConfigurableApplicationContext prefabContext = new PrefabWebApplicationContext(prefab, context, servletContext);
        prefabRegistry.addPrefabContext(prefab.getName(), prefabContext);
    }

    @Autowired
    private PrefabManager prefabManager;

    @Autowired
    private PrefabRegistry prefabRegistry;
    private ServletContext servletContext;

    private ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext)
            throws BeansException {
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

    @Override
    public void onApplicationEvent(final PrefabEvent event) {
        if (event instanceof PrefabsLoadedEvent) {
            if (event.getSource() == context) {
                installPrefabs();
            }
        } else if (event instanceof PrefabsUnloadedEvent) {
            if (event.getSource() == context) {
                uninstallPrefabs();
            }
        }
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
            for (String prefabName : prefabRegistry.getPrefabs()) {
                prefabRegistry.getPrefabContext(prefabName).close();
            }
            prefabRegistry.deletePrefabContexts();
        }

        if (prefabManager != null) {
            // closing class loader
            for (Prefab prefab : prefabManager.getPrefabs()) {
                ((PrefabClassLoader) prefab.getClassLoader()).close();
                prefab.setClassLoader(null);
            }
            prefabManager.deleteAllPrefabs();
        }
    }
}
