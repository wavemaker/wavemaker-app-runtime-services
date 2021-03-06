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
package com.wavemaker.runtime;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import com.wavemaker.runtime.app.AppFileSystem;
import com.wavemaker.runtime.prefab.context.PrefabThreadLocalContextManager;

/**
 * This singleton class is to store any properties in the scope of the application context and its prefabs context.
 *
 * @author Seung Lee
 * @author Jeremy Grelle
 */
public class WMAppContext implements ApplicationContextAware, ServletContextAware {

    private String applicationHostUrl;

    private int applicationHostPort;

    private boolean secured;

    private boolean initialized;

    private PrefabThreadLocalContextManager prefabThreadLocalContextManager;

    private ApplicationContext rootApplicationContext;

    private ServletContext servletContext;

    private static WMAppContext instance;

    private WMAppContext() {
        instance = this;
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public String getAppContextRoot() {
        return getSpringBean(AppFileSystem.class).getAppContextRoot();
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
            PrefabThreadLocalContextManager prefabThreadLocalContextManager = null;
            try {
                prefabThreadLocalContextManager = getRootApplicationContext()
                        .getBean(PrefabThreadLocalContextManager.class);
            } catch (NoSuchBeanDefinitionException e) {
                prefabThreadLocalContextManager = new PrefabThreadLocalContextManager(); //To prevent this method being called every time
            }
            this.prefabThreadLocalContextManager = prefabThreadLocalContextManager;
        }
        return this.prefabThreadLocalContextManager;
    }

    private ApplicationContext getRootApplicationContext() {
        return rootApplicationContext;
    }

    public void init(HttpServletRequest httpServletRequest) {
        if (!initialized) {
            applicationHostUrl = httpServletRequest.getServerName();
            applicationHostPort = httpServletRequest.getServerPort();
            secured = httpServletRequest.isSecure();
            initialized = true;
        }
    }

    public String getApplicationHostUrl() {
        return applicationHostUrl;
    }

    public int getApplicationHostPort() {
        return applicationHostPort;
    }

    public boolean isSecured() {
        return secured;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.rootApplicationContext = applicationContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
