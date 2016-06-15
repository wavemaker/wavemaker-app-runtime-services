/**
 * Copyright © 2013 - 2016 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.server;

import java.beans.Introspector;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sun.jndi.ldap.Connection;
import com.sun.jndi.ldap.LdapClient;
import com.sun.jndi.ldap.LdapPoolManager;
import com.sun.naming.internal.ResourceManager;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.wavemaker.runtime.WMAppContext;
import com.wavemaker.studio.common.classloader.ClassLoaderUtils;
import com.wavemaker.studio.common.util.CastUtils;
import com.wavemaker.studio.common.util.IOUtils;
import com.wavemaker.studio.common.util.WMUtils;

/**
 * Listener that flushes all of the Introspector's internal caches and de-registers all JDBC drivers on web app
 * shutdown.
 *
 * @author Frankie Fu
 * @author akritim
 */
public class CleanupListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(CleanupListener.class);

    private boolean isSharedLib() {
        return WMUtils.isSharedLibSetup();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        //properties set to time out LDAP connections automatically
        System.setProperty("com.sun.jndi.ldap.connect.pool.timeout", "2000");
        System.setProperty("ldap.connection.com.sun.jndi.ldap.read.timeout", "1000");
        WMAppContext.init(event);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            /**
             * Deregistering drivers has the side effect of registering driver classes
             * which are there in other class loaders but not yet loaded in the current class loader.
             *
             * De registering it at the start so that preceding clean up tasks may clean any references created by loading unwanted classes by this call.
             */
            deregisterDrivers();
            //Release references that are not being closed automatically by libraries
            shutDownHSQLTimerThreadIfAny();
            shutDownMySQLThreadIfAny();
            deRegisterOracleDiagnosabilityMBean();
            typeFactoryClearTypeCache();
            resourceManagerClearPropertiesCache();
            clearReaderArrCatalogManager();
            clearCacheSourceAbstractClassGenerator();
            clearThreadConnections();

            //Release all open references for logging
            LogFactory.release(this.getClass().getClassLoader());

            // flush all of the Introspector's internal caches
            Introspector.flushCaches();
            logger.info("Clean Up Successful!");
        } catch (Exception e) {
            logger.info("Failed to clean up some things on app undeploy", e);
        } finally {
            WMAppContext.clearInstance();
        }
    }

    /**
     * Added by akritim
     * To stop HSQL timer thread, if any
     */
    private void shutDownHSQLTimerThreadIfAny() {
        String className = "org.hsqldb.DatabaseManager";
        try {
            Class klass = ClassLoaderUtils.findLoadedClass(Thread.currentThread().getContextClassLoader(), className);
            if (klass != null && klass.getClassLoader() == this.getClass().getClassLoader()) {
                //Shutdown the thread only if the class is loaded by web-app
                final Class<?> databaseManagerClass = ClassUtils.getClass("org.hsqldb.DatabaseManager");
                final Class<?> hsqlTimerClass = ClassUtils.getClass("org.hsqldb.lib.HsqlTimer");

                Method timerMethod = databaseManagerClass.getMethod("getTimer");

                Object timerObj = timerMethod.invoke(null);
                if (timerObj != null) {
                    hsqlTimerClass.getMethod("shutDown").invoke(timerObj);

                    Thread hsqlTimerThread = (Thread) hsqlTimerClass.getMethod("getThread").invoke(timerObj);
                    if (hsqlTimerThread != null && hsqlTimerThread.isAlive()) {
                        logger.info("Joining HSQL-Timer thread: {}", hsqlTimerThread.getName());
                        hsqlTimerThread.join(2000);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("Failed to shutdown hsql timer thread {}", className, e);
        }
    }

    /**
     * Added by akritim
     * To stop mysql thread, if any and resolve issue of "Abandoned connection cleanup thread" not stopping
     */
    private void shutDownMySQLThreadIfAny() {
        String className = "com.mysql.jdbc.AbandonedConnectionCleanupThread";
        try {
            Class<?> klass = ClassLoaderUtils.findLoadedClass(Thread.currentThread().getContextClassLoader(),
                    className);
            if (klass != null && klass.getClassLoader() == this.getClass().getClassLoader()) {
                //Shutdown the thread only if the class is loaded by web-app
                logger.info("Shutting down mysql AbandonedConnectionCleanupThread");
                klass.getMethod("shutdown").invoke(null);
            }
        } catch (Throwable e) {
            logger.warn("Failed to shutdown mysql thread {}", className, e);
        }
    }

    /**
     * De Registers the mbean registered by the oracle driver
     */
    private void deRegisterOracleDiagnosabilityMBean() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String mBeanName = cl.getClass().getName() + "@" + Integer.toHexString(cl.hashCode());
        try {
            try {
                deRegisterOracleDiagnosabilityMBean(mBeanName);
            } catch (InstanceNotFoundException e) {
                logger.debug("Oracle OracleDiagnosabilityMBean {} not found", mBeanName, e);
                //Trying with different mBeanName as some versions of oracle driver uses the second formula for mBeanName
                mBeanName = cl.getClass().getName() + "@" + Integer.toHexString(cl.hashCode()).toLowerCase();
                try {
                    deRegisterOracleDiagnosabilityMBean(mBeanName);
                } catch (InstanceNotFoundException e1) {
                    logger.debug("Oracle OracleDiagnosabilityMBean {} also not found", mBeanName, e);
                }
            }
        } catch (Throwable e) {
            logger.error("Oracle JMX unregistration error", e);
        }
    }

    private void deRegisterOracleDiagnosabilityMBean(String nameValue) throws InstanceNotFoundException, MBeanRegistrationException, MalformedObjectNameException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final Hashtable<String, String> keys = new Hashtable<String, String>();
        keys.put("type", "diagnosability");
        keys.put("name", nameValue);
        mbs.unregisterMBean(new ObjectName("com.oracle.jdbc", keys));
        logger.info("Deregistered OracleDiagnosabilityMBean {}", nameValue);
    }

    /**
     * Added by akritim
     * To clear TypeFactory's TypeCache
     */
    private void typeFactoryClearTypeCache() {
        if (isSharedLib()) {
            String className = "com.fasterxml.jackson.databind.type.TypeFactory";
            try {
                Class klass = ClassLoaderUtils
                        .findLoadedClass(Thread.currentThread().getContextClassLoader().getParent(), className);
                if (klass != null) {
                    logger.info("Attempt to clear typeCache from {} class instance", klass);
                    TypeFactory.defaultInstance().clearCache();
                }
            } catch (Throwable e) {
                logger.warn("Failed to Clear TypeCache from {}", className, e);
            }
        }
    }

    /**
     * Added by akritim
     * To clear ReaderArr in CatalogManager
     */
    private void clearReaderArrCatalogManager() {
        try {
            logger.info("Attempt to clear readerArr field of type Vector from class {}", Catalog.class);
            Catalog catalog = CatalogManager.getStaticManager().getCatalog();
            Field readerArrField = Catalog.class.getDeclaredField("readerArr");
            readerArrField.setAccessible(true);
            Vector reader = (Vector) readerArrField.get(catalog);
            if (reader != null) {
                reader.clear();
            }
        } catch (Throwable e) {
            logger.warn("Failed to clear readArr from catalog", e);
        }
    }

    /**
     * Added by akritim
     * To clear ResourceManager's PropertiesCache
     */
    private void resourceManagerClearPropertiesCache() {
        Class<ResourceManager> klass = ResourceManager.class;
        try {
            Field propertiesCache = klass.getDeclaredField("propertiesCache");
            propertiesCache.setAccessible(true);
            WeakHashMap<Object, Hashtable<? super String, Object>> map = (WeakHashMap<Object, Hashtable<? super String, Object>>) propertiesCache
                    .get(null);
            if (!map.isEmpty()) {
                logger.info("Clearing propertiesCache from ");
                map.clear();
            }
        } catch (Throwable e) {
            logger.warn("Failed to clear propertiesCache from {}", klass, e);
        }
    }

    /**
     * Added by akritim
     * To clear cache from AbtractClassGenerator's Source
     */
    private void clearCacheSourceAbstractClassGenerator() {
        if (isSharedLib()) {
            try {
                String className = "org.springframework.cglib.core.AbstractClassGenerator$Source";
                logger.info("Attempt to clear cache field from class {}", className);
                Field SOURCE = Enhancer.class.getDeclaredField("SOURCE");
                SOURCE.setAccessible(true);
                SOURCE.get(null);
                Field cache = org.springframework.cglib.core.AbstractClassGenerator.class.getClassLoader()
                        .loadClass(className).getDeclaredField("cache");
                cache.setAccessible(true);
                Map map = (Map) cache.get(SOURCE.get(null));
                map.remove(CleanupListener.class.getClassLoader());
            } catch (Throwable e) {
                logger.warn("Failed to Clear Cache from Source", e);
            }
        }
    }

    private void clearThreadConnections() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread.isAlive() && !(thread == Thread.currentThread()) &&
                    (thread.getContextClassLoader() == Thread.currentThread().getContextClassLoader())) {
                try {
                    if (thread.getName().startsWith("C3P0PooledConnectionPoolManager") && thread.getName().toLowerCase()
                            .contains("helper")) {
                        logger.info("Joining C3P0PooledConnectionPoolManager Helper Thread {}", thread);
                        thread.join(2000);
                    } else if (thread.getName().startsWith("Thread-")) {
                        Field targetField = Thread.class.getDeclaredField("target");
                        targetField.setAccessible(true);
                        Runnable runnable = (Runnable) targetField.get(thread);
                        if (runnable != null && runnable instanceof Connection) {
                            logger.info("Interrupting LDAP connection thread");
                            Connection conn = (Connection) runnable;
                            IOUtils.closeSilently(conn.inStream);
                            IOUtils.closeSilently(conn.outStream);
                            Field parent = Connection.class.getDeclaredField("parent");
                            parent.setAccessible(true);
                            LdapClient ldapClient = (LdapClient) parent.get(conn);
                            ldapClient.closeConnection();
                            LdapPoolManager.expire(3000);
                            if (!thread.isInterrupted()) {
                                thread.stop();
                            }
                        }
                    }
                } catch (Throwable t) {
                    logger.warn("Failed to stop the thread {} properly", thread, t);
                }
            }
        }
    }

    /**
     * Added by akritim
     * To de-registed drivers loaded by current web-app
     */
    private void deregisterDrivers() {
        try {
            // remove from the system DriverManager the JDBC drivers registered
            // by this web app
            /** Adding this line as getDrivers has a side effect of registering drivers
             * that are visible to this class loader but haven't yet been loaded and the newly registered
             * drivers are not returned in the call,therefore calling
             * DriverManager.getDriviers() twice to get the full list including the newly registered drivers
             **/
            Enumeration<Driver> ignoreDrivers = DriverManager.getDrivers();
            for (Enumeration<Driver> e = CastUtils.cast(DriverManager.getDrivers()); e.hasMoreElements(); ) {
                Driver driver = e.nextElement();
                if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
                    logger.info("De Registering the driver {}", driver.getClass().getCanonicalName());
                    try {
                        DriverManager.deregisterDriver(driver);
                    } catch (SQLException e1) {
                        logger.warn("Failed to de-register driver ", driver.getClass().getCanonicalName(), e1);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("Failed to de-register drivers", e);
        }
    }
}
