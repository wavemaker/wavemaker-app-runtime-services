/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.memory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.lang.reflect.Field;
import java.util.List;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clears any notification listeners registered with memory mx bean
 * For example Oracle's BlockSource creates a mbean listener which needs to be deregistered
 */
public class CleanupMBeanNotificationListener implements MemoryLeakPreventionListener {

    private static final Logger logger = LoggerFactory.getLogger(CleanupMBeanNotificationListener.class);
    private final boolean sunManagementPackageOpen = isSunManagementPackageOpen();

    @Override
    public void listen(ClassLoader classLoader) {
        if (!sunManagementPackageOpen) {
            logger.info("Skipping MBean clean up as java.management/sun.management is not open for unnamed modules");
            return;
        }
        cleanupNotificationListener(classLoader, ManagementFactory.getMemoryMXBean());
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            cleanupNotificationListener(classLoader, garbageCollectorMXBean);
        }
    }

    @Override
    public int getOrder() {
        return 300;
    }

    private void cleanupNotificationListener(
        ClassLoader classLoader, PlatformManagedObject platformManagedObject) {
        try {
            NotificationEmitter notificationEmitter = (NotificationEmitter) platformManagedObject;
            Field listenerListField = ClassUtils.findField(notificationEmitter.getClass(), "listenerList");
            if (listenerListField == null) {
                logger.warn("Unrecognized field listenerList in NotificationEmitter");
                return;
            }
            List listenerInfoList = (List) listenerListField
                .get(notificationEmitter); //This object would be List<ListenerInfo>
            for (Object o : listenerInfoList) {
                Field listenerField = ClassUtils.findField(o.getClass(), "listener");
                if (listenerField == null) {
                    logger.warn("Unrecognized field listener in ListenerInfo");
                    return;
                }
                NotificationListener notificationListener = (NotificationListener) listenerField.get(o);
                if (notificationListener.getClass().getClassLoader() == classLoader) {
                    logger.info("Removing registered mBean notification listener {}",
                        notificationListener.getClass().getName());
                    notificationEmitter.removeNotificationListener(notificationListener);
                }
            }
        } catch (Exception e) {
            logger.warn("MBean clean up is not successful, any uncleared notification listeners might create a memory leak", e);
        }
    }

    private boolean isSunManagementPackageOpen() {
        Module javaManagementModule = NotificationEmitter.class.getModule();
        Module unnamedModule = CleanupMBeanNotificationListener.class.getModule();
        return javaManagementModule.isOpen("sun.management", unnamedModule);
    }
}
