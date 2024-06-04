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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.hsqldb.DatabaseManager;
import org.hsqldb.lib.HsqlTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.io.DeleteTempFileOnCloseInputStream;

/**
 * Listener that flushes all of the Introspector's internal caches and de-registers all JDBC drivers on web app
 * shutdown.
 *
 * @author Frankie Fu
 * @author akritim
 */
public class CleanupListener implements ServletContextListener {

    private static Logger logger;

    private static final int MAX_WAIT_TIME_FOR_RUNNING_THREADS = Integer
        .getInteger("wm.app.maxWaitTimeRunningThreads", 5000);
    private static final boolean SUN_MANAGEMENT_PACKAGE_OPEN = isSunManagementPackageOpen();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            /**
             * De registering it at the start so that preceding clean up tasks may clean any references created by loading unwanted classes by this call.
             */
            deregisterDrivers(getAppClassLoader());
            deRegisterOracleDiagnosabilityMBean(getAppClassLoader());
            cleanupMBeanNotificationListeners(getAppClassLoader());
            deregisterSecurityProviders(getAppClassLoader());
            DeleteTempFileOnCloseInputStream.TempFileManager.stopScheduler();
            stopRunningThreads(getAppClassLoader(), MAX_WAIT_TIME_FOR_RUNNING_THREADS);
            logger.info("Clean Up Successful!");
        } catch (Exception e) {
            logger.info("Failed to clean up some things on app undeploy", e);
        }
    }

    /**
     * Added by akritim
     * To stop HSQL timer thread, if any
     */
    private static void shutDownHSQLTimerThreadIfAny() {
        try {
            HsqlTimer timer = DatabaseManager.getTimer();
            timer.shutDown();
        } catch (NoClassDefFoundError e) {
            logger.debug("Hsql classes not found in classpath, skipping hsql timer task");
        } catch (Throwable e) {
            logger.warn("Couldn't stop hsql's HsqlTimer thread", e);
        }
    }

    /**
     * Added by akritim
     * To stop mysql thread, if any and resolve issue of "Abandoned connection cleanup thread" not stopping
     */
    private static void shutDownMySQLThreadIfAny() {
        //For mysql driver(version 5.1.41+)
        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (NoClassDefFoundError e) {
            logger.debug("Mysql classes not found in classpath, skipping shutdown mysql thread task");
        } catch (Throwable e) {
            logger.warn("Couldn't stop mysql's AbandonedConnectionCleanupThread", e);
        }
    }

    /**
     * De Registers the mbean registered by the oracle driver
     */
    public static void deRegisterOracleDiagnosabilityMBean(ClassLoader classLoader) {
        String mBeanName = classLoader.getClass().getName() + "@" + Integer.toHexString(classLoader.hashCode());
        try {
            try {
                deRegisterOracleDiagnosabilityMBean(mBeanName);
            } catch (InstanceNotFoundException e) {
                logger.debug("Oracle OracleDiagnosabilityMBean {} not found", mBeanName, e);
                //Trying with different mBeanName as some versions of oracle driver uses the second formula for mBeanName
                mBeanName = classLoader.getClass().getName() + "@" + Integer.toHexString(classLoader.hashCode())
                    .toLowerCase();
                try {
                    deRegisterOracleDiagnosabilityMBean(mBeanName);
                } catch (InstanceNotFoundException e1) {
                    logger.debug("Oracle OracleDiagnosabilityMBean {} also not found", mBeanName);
                }
            }
        } catch (Throwable e) {
            logger.error("Oracle JMX unregistration error", e);
        }
    }

    private static void deRegisterOracleDiagnosabilityMBean(String nameValue)
        throws InstanceNotFoundException, MBeanRegistrationException, MalformedObjectNameException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final Hashtable<String, String> keys = new Hashtable<>();
        keys.put("type", "diagnosability");
        keys.put("name", nameValue);
        mbs.unregisterMBean(new ObjectName("com.oracle.jdbc", keys));
        logger.info("Deregistered OracleDiagnosabilityMBean {}", nameValue);
    }

    /**
     * Clears any notification listeners registered with memory mx bean
     * For example Oracle's BlockSource creates an mbean listener which needs to be deregistered
     */
    public static void cleanupMBeanNotificationListeners(ClassLoader classLoader) {
        if (!SUN_MANAGEMENT_PACKAGE_OPEN) {
            logger.info("Cannot perform MBean clean up as java.management/sun.management is not open for unnamed modules");
            return;
        }
        cleanupNotificationListener(classLoader, ManagementFactory.getMemoryMXBean());
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            cleanupNotificationListener(classLoader, garbageCollectorMXBean);
        }
    }

    private static void cleanupNotificationListener(
        ClassLoader classLoader, PlatformManagedObject platformManagedObject) {
        try {
            NotificationEmitter notificationEmitter = (NotificationEmitter) platformManagedObject;
            Field listenerListField = findField(notificationEmitter.getClass(), "listenerList");
            if (listenerListField == null) {
                throw new WMRuntimeException(
                    MessageResource.create("com.wavemaker.runtime.unrecognized.notificationEmitter"),
                    notificationEmitter.getClass().getName());
            }
            List listenerInfoList = (List) listenerListField
                .get(notificationEmitter); //This object would be List<ListenerInfo>
            for (Object o : listenerInfoList) {
                Field listenerField = findField(o.getClass(), "listener");
                if (listenerListField == null) {
                    throw new WMRuntimeException(
                        MessageResource.create("com.wavemaker.runtime.unrecognizedListenerInfo"),
                        o.getClass().getName());
                }
                if (listenerField != null) {
                    NotificationListener notificationListener = (NotificationListener) listenerField.get(o);
                    if (notificationListener.getClass().getClassLoader() == classLoader) {
                        logger.info("Removing registered mBean notification listener {}",
                            notificationListener.getClass().getName());
                        notificationEmitter.removeNotificationListener(notificationListener);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("MBean clean up is not successful, any uncleared notification listeners might create a memory leak", e);
        }
    }

    /**
     * de-registers the JDBC drivers registered visible to this class loader from DriverManager
     * Added by akritim
     */
    private void deregisterDrivers(ClassLoader classLoader) {
        try {

            /*
             * DriverManager.getDrivers() has the side effect of registering driver classes
             * which are there in other class loaders(and registered with DriverManager) but not yet loaded in the caller class loader.
             * So calling it twice so that the second call to getDrivers will actually return all the drivers visible to the caller class loader.
             * Synchronizing the process to prevent a rare case where the second call to getDrivers method actually registers the unwanted driver
             * because of registerDriver from some other thread between the two getDrivers call
             */
            Enumeration<Driver> drivers;
            synchronized (DriverManager.class) {
                Enumeration<Driver> ignoreDrivers = DriverManager.getDrivers();
                drivers = DriverManager.getDrivers();
            }
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == classLoader) {
                    logger.info("De Registering the driver {}", driver.getClass().getCanonicalName());
                    try {
                        DriverManager.deregisterDriver(driver);
                    } catch (SQLException e1) {
                        logger.warn("Failed to de-register driver {}", driver.getClass().getCanonicalName(), e1);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("Failed to de-register drivers", e);
        }
    }

    public static void deregisterSecurityProviders(ClassLoader classLoader) {
        logger.info("Attempting to deregister any security providers registered by webapp");
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            if (provider.getClass().getClassLoader() == classLoader) {
                logger.info("De registering security provider {} with name {} which is registered in the class loader",
                    provider, provider.getName());
                Security.removeProvider(provider.getName());
            }
        }

    }

    /**
     * Will interrupt all the running threads in the given class loader except current thread.
     * Post interrupt after a specific timeout if any threads are still alive it logs a message
     */
    public static void stopRunningThreads(ClassLoader classLoader, long waitTimeOutInMillis) {
        shutDownMySQLThreadIfAny();
        shutDownHSQLTimerThreadIfAny();
        try {
            List<Thread> threads = getThreads(classLoader);
            List<Thread> runningThreads = new ArrayList<>();
            for (Thread thread : threads) {
                if (isAliveAndNotCurrentThread(thread)) {
                    logger.info("Interrupting thread {}", thread);
                    thread.interrupt();
                    runningThreads.add(thread);
                }
                stopTimerThread(thread);
            }
            if (!runningThreads.isEmpty()) {
                logger.info("Waiting for interrupted threads to be finished in max of {} ms", waitTimeOutInMillis);
                join(runningThreads, waitTimeOutInMillis);
                for (Thread thread : runningThreads) {
                    if (thread.isAlive()) {
                        StackTraceElement[] stackTrace = thread.getStackTrace();
                        Throwable throwable = new IllegalThreadStateException(
                            "Thread [" + thread.getName() + "] is Still running");
                        throwable.setStackTrace(stackTrace);
                        logger.warn(
                            "Thread {} is still alive after waiting for {} and will mostly probably create a memory leak",
                            thread.getName(),
                            waitTimeOutInMillis, throwable);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed in stopRunningThreads", e);
        }
    }

    private static void stopTimerThread(Thread thread) {
        if (!thread.getClass().getName().startsWith("java.util.Timer")) {
            return;
        }
        logger.info("Stopping Timer thread {}", thread);
        Field newTasksMayBeScheduled = findField(thread.getClass(), "newTasksMayBeScheduled");
        Field queueField = findField(thread.getClass(), "queue");
        if (queueField != null && newTasksMayBeScheduled != null) {
            try {
                Object queue = queueField.get(thread);
                newTasksMayBeScheduled.set(thread, false);
                Method clearMethod = findMethod(queue.getClass(), "clear");
                synchronized (queue) {
                    if (clearMethod != null) {
                        clearMethod.invoke(queue);
                    }
                    newTasksMayBeScheduled.set(thread, false);
                    queue.notify();
                }
            } catch (Exception e) {
                logger.warn("Failed to stop timer thread {}", thread, e);
            }
        } else {
            logger.warn(
                "Couldn't stop timer thread {} as one of newTasksMayBeScheduled/queue fields are not present in the class {}",
                thread, thread
                    .getClass().getName());
        }

    }

    /**
     * returns threads running in the given in class loader context or whose class is loaded from given class loader
     */
    private static List<Thread> getThreads(ClassLoader classLoader) {
        return Thread.getAllStackTraces().keySet().stream().filter(thread -> {
            return thread.getContextClassLoader() == classLoader || thread.getClass().getClassLoader() == classLoader;
        }).collect(Collectors.toList());
    }

    private static boolean isAliveAndNotCurrentThread(Thread thread) {
        return thread.isAlive() && thread != Thread.currentThread();
    }

    private static synchronized void join(List<Thread> threads, long millis) throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        } else if (millis == 0) {
            for (Thread thread : threads) {
                thread.join();
            }
        } else {

            long base = System.currentTimeMillis();
            long now = 0;

            for (Thread thread : threads) {
                while (thread.isAlive()) {
                    long delay = millis - now;
                    if (delay <= 0) {
                        break;
                    }
                    thread.join(delay);
                    now = System.currentTimeMillis() - base;
                }
            }
        }
    }

    private static ClassLoader getAppClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static Field findField(Class klass, String name) {
        Field field = ReflectionUtils.findField(klass, name);
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
        }
        return field;
    }

    private static Method findMethod(Class klass, String name, Class... paramTypes) {
        Method method = ReflectionUtils.findMethod(klass, name, paramTypes);
        if (method != null) {
            ReflectionUtils.makeAccessible(method);
        }
        return method;
    }

    public static void init() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(CleanupListener.class);
        }
    }

    private static boolean isSunManagementPackageOpen() {
        Module javaManagementModule = NotificationEmitter.class.getModule();
        Module unnamedModule = CleanupListener.class.getModule();
        return javaManagementModule.isOpen("sun.management", unnamedModule);
    }

}
