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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.MDC;

import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.web.filter.WMRequestFilter;

/**
 * This works in conjuction with {@link LoggingInitializationListener} class which is the first class loaded in web.xml.
 * 
 * This is the last listener declared in the web.xml.
 * 
 * It removes the mdc value set by {@link LoggingInitializationListener} in its contextInitialized method as it will be the last method to be called.
 * 
 * Its contextDestroyed method is the first to be called during undeployment and it creates the mdc value which is removed in contextDestroye method of
 * {@link LoggingInitializationListener} class
 * 
 * 
 * @author Uday Shankar
 */
public class AppNameMDCStartStopListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (RuntimeEnvironment.isTestRunEnvironment()) {
            MDC.remove(WMRequestFilter.APP_NAME_KEY);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (RuntimeEnvironment.isTestRunEnvironment()) {
            MDC.put(WMRequestFilter.APP_NAME_KEY, sce.getServletContext().getContextPath());
        }
    }
}
