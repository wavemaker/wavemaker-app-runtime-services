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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.MDC;

import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.web.filter.WMRequestFilter;

/**
 * This class provides a workaround to the staticLog Issue mentioned in the below link.
 * <a href="http://wiki.apache.org/commons/Logging/StaticLog">http://wiki.apache.org/commons/Logging/StaticLog</a>
 *
 * <p>
 *
 * This workaround lets each application define their own logging configuration. Below are the points to be noted.
 * <ul>
 *     <li>This is enabled only in testRun environment where shared lib approach is used and multiple applications are run on the same server</li>
 *     <li>This class reloads logging configuration each time an application is deployed.</li>
 *     <li>With this each app can have its own logging configuration instead of a single configuration fetched from the first application's configuration
 * when slf4j and log4j related jars are present in shared lib.</li>
 *     <li>Loggers used by the classes in the shared lib are also reconfigured to use the latest app's configuration</li>
 *     <li>If two applications are running simultaneously, the configuration set by the latest ran application will be used</li>
 *     <li>One issue is that the configuration in the old running app will be updated the new configuration changes</li>
 * </ul>
 * </p>
 *
 * It works in conjuction with {@link AppNameMDCStartStopListener} class for starting and stopping mdc variables. Check its documentation for more details.
 *
 * @author Uday Shankar
 */
public class LoggingInitializationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (RuntimeEnvironment.isTestRunEnvironment()) {
            MDC.put(WMRequestFilter.APP_NAME_KEY, sce.getServletContext().getContextPath());

            try {
                System.out.println("Reinitializing log4j configuration");

                //Reconfiguring the loggerContext to load the updated changes if there are any in a logger file(ie log4j2.properties or log4j2.json or log4j2.xml)
                LoggerContext loggerContext = (LoggerContext) LogManager.getContext(getClass().getClassLoader().getParent(), false);
                loggerContext.reconfigure();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to initialize log4j logging");
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (RuntimeEnvironment.isTestRunEnvironment()) {
            MDC.remove(WMRequestFilter.APP_NAME_KEY);
        }
    }
}
