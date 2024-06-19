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
package com.wavemaker.runtime.web.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import com.wavemaker.app.memory.MemoryLeakPreventionHandler;
import com.wavemaker.app.memory.MemoryLeakPreventionHandlerFactory;
import com.wavemaker.commons.io.DeleteTempFileOnCloseInputStream;

/**
 * Listener that flushes all of the Introspector's internal caches and de-registers all JDBC drivers on web app
 * shutdown.
 *
 * @author Frankie Fu
 * @author akritim
 */
public class CleanupListener implements ServletContextListener {

    private MemoryLeakPreventionHandler memoryLeakPreventionHandler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        memoryLeakPreventionHandler = new MemoryLeakPreventionHandlerFactory().getMemoryLeakPreventionHandler(event);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        DeleteTempFileOnCloseInputStream.TempFileManager.stopScheduler();
        memoryLeakPreventionHandler.handle(getAppClassLoader());
    }

    private ClassLoader getAppClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}