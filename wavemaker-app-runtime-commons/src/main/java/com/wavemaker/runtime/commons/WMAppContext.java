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
package com.wavemaker.runtime.commons;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import com.wavemaker.runtime.prefab.context.PrefabThreadLocalContextManager;

/**
 * This singleton class is to store any properties in the scope of the application context and its prefabs context.
 *
 * @author Seung Lee
 * @author Jeremy Grelle
 */
public class WMAppContext implements ApplicationContextAware, ServletContextAware {

    private PrefabThreadLocalContextManager prefabThreadLocalContextManager;

    private ApplicationContext rootApplicationContext;

    private ServletContext servletContext;

    private static WMAppContext instance;

    private WMAppContext() {
        if (instance == null) {
            instance = this;
        }
    }

    public static synchronized WMAppContext getInstance() {
        return instance;
    }

    /**
     * Used by old file service java classes
     * @return
     */
    @Deprecated
    public ServletContext getContext() {
        return servletContext;
    }

    public <T> T getSpringBean(String beanId) {
        ApplicationContext applicationContext = detectCurrentApplicationContext();
        return (T) applicationContext.getBean(beanId);
    }

    public <T> T getSpringBean(Class<T> c) {
        ApplicationContext applicationContext = detectCurrentApplicationContext();
        return applicationContext.getBean(c);
    }

    private ApplicationContext detectCurrentApplicationContext() {
        PrefabThreadLocalContextManager prefabThreadLocalContextManager = getPrefabThreadLocalContextManager();
        ApplicationContext applicationContext = getRootApplicationContext();
        if (prefabThreadLocalContextManager != null) {
            ApplicationContext context = prefabThreadLocalContextManager.getContext();
            if (context != null) {
                applicationContext = context;
            }
        }
        return applicationContext;
    }

    private PrefabThreadLocalContextManager getPrefabThreadLocalContextManager() {
        if (prefabThreadLocalContextManager == null) {//Locking not really needed
            PrefabThreadLocalContextManager prefabThreadLocalContextManager;
            try {
                prefabThreadLocalContextManager = getRootApplicationContext()
                        .getBean(PrefabThreadLocalContextManager.class);
            } catch (NoSuchBeanDefinitionException e) {
                prefabThreadLocalContextManager = new PrefabThreadLocalContextManager(); //To prevent this method being called every time
            }
            this.prefabThreadLocalContextManager = prefabThreadLocalContextManager;
        }
        return prefabThreadLocalContextManager;
    }

    private ApplicationContext getRootApplicationContext() {
        return rootApplicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        rootApplicationContext = applicationContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
