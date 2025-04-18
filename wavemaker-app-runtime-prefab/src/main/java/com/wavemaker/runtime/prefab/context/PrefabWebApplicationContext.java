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
package com.wavemaker.runtime.prefab.context;

import jakarta.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.wavemaker.runtime.prefab.core.Prefab;

/**
 * @author Dilip Kumar
 */
public class PrefabWebApplicationContext extends XmlWebApplicationContext {

    public PrefabWebApplicationContext(final Prefab prefab, final ApplicationContext parent,
                                       final ServletContext servletContext) {
        setId(prefab.getName());
        setParent(parent);

        // This removes the parent properties added and creates new property sources, as we want this context to have its own property sources and not get the parent properties
        setEnvironment(new StandardServletEnvironment());

        ClassLoader prefabClassLoader = prefab.getClassLoader();
        setClassLoader(prefabClassLoader);
        setServletContext(servletContext);
        setDisplayName("Prefab Context [" + prefab.getName() + "]");
        setConfigLocations("classpath:prefab-springapp.xml");
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(prefabClassLoader);
            refresh();
        } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
        }
    }
}
