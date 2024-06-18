/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.memory;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public class MemoryLeakPreventionHandlerFactory {

    public MemoryLeakPreventionHandler getMemoryLeakPreventionHandler(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        Object memoryLeakPreventionHandler = servletContext.getAttribute("memoryLeakPreventionHandler");
        if (memoryLeakPreventionHandler == null) {
            memoryLeakPreventionHandler = new MemoryLeakPreventionHandler();
            ((MemoryLeakPreventionHandler) memoryLeakPreventionHandler).registerListeners(getDefaultMemoryLeakListeners());
            servletContext.setAttribute("memoryLeakPreventionHandler", memoryLeakPreventionHandler);
        }
        return (MemoryLeakPreventionHandler) memoryLeakPreventionHandler;
    }

    private List<MemoryLeakPreventionListener> getDefaultMemoryLeakListeners() {
        return Arrays.asList(
            new DeRegisterDriversListener(), new CleanupMBeanNotificationListener(), new DeRegisterSecurityProvidersListener(),
            new StopRunningThreadsListener()
        );
    }
}
