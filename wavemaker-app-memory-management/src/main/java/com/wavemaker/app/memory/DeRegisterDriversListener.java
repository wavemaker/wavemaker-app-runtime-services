/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.memory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * De-registers the JDBC drivers registered visible to this class loader from DriverManager
 */
public class DeRegisterDriversListener implements MemoryLeakPreventionListener {

    private static final Logger logger = LoggerFactory.getLogger(DeRegisterDriversListener.class);

    /**
     * DriverManager.getDrivers() has the side effect of registering driver classes
     * which are there in other class loaders(and registered with DriverManager) but not yet loaded in the caller class loader.
     * So calling it twice so that the second call to getDrivers will actually return all the drivers visible to the caller class loader.
     * Synchronizing the process to prevent a rare case where the second call to getDrivers method actually registers the unwanted driver
     * because of registerDriver from some other thread between the two getDrivers call
     */
    @Override
    public void listen(ClassLoader classLoader) {
        try {
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

    @Override
    public int getOrder() {
        return 100;
    }
}
