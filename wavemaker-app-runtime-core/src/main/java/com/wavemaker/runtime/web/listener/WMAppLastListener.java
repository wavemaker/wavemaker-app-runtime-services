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

import com.wavemaker.runtime.web.filter.WMRequestFilter;

/**
 * This is the last listener declared in the web.xml.
 *
 * It removes the mdc value set by {@link WMAppFirstListener} in its contextInitialized method as it will be the last method to be called.
 *
 * This works in conjunction with {@link WMAppFirstListener} class which is the first listener defined in the web application.
 *
 * Its contextDestroyed method is the first to be called during un deployment, and it sets the mdc value which is removed in contextDestroyed method of
 * {@link WMAppFirstListener} class
 *
 * @author Uday Shankar
 */
public class WMAppLastListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MDC.remove(WMRequestFilter.APP_NAME_KEY);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MDC.put(WMRequestFilter.APP_NAME_KEY, sce.getServletContext().getContextPath());
    }
}
