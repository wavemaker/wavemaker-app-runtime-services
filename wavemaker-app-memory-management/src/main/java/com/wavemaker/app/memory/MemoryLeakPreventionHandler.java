/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.memory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

public class MemoryLeakPreventionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MemoryLeakPreventionHandler.class);
    private final List<MemoryLeakPreventionListener> memoryLeakPreventionListeners = new ArrayList<>();

    public void registerListeners(List<MemoryLeakPreventionListener> memoryLeakPreventionListeners) {
        this.memoryLeakPreventionListeners.addAll(memoryLeakPreventionListeners);
        OrderComparator.sort(this.memoryLeakPreventionListeners);
    }

    public void handle(ClassLoader classLoader) {
        memoryLeakPreventionListeners.forEach(memoryLeakPreventionListener -> {
            try {
                logger.info("Executing listener: {}", memoryLeakPreventionListener.getClass().getSimpleName());
                memoryLeakPreventionListener.listen(classLoader);
            } catch (Throwable e) {
                logger.info("Failed to execute listener {}", memoryLeakPreventionListener.getClass().getSimpleName());
            }
        });
        logger.info("Executed all memory leak prevention listeners");
    }
}
