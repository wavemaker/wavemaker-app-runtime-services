/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.memory;

import java.security.Provider;
import java.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeRegisterSecurityProvidersListener implements MemoryLeakPreventionListener {

    private static final Logger logger = LoggerFactory.getLogger(DeRegisterSecurityProvidersListener.class);

    @Override
    public void listen(ClassLoader classLoader) {
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            if (provider.getClass().getClassLoader() == classLoader) {
                logger.info("De registering security provider {} with name {} which is registered in the class loader",
                    provider, provider.getName());
                Security.removeProvider(provider.getName());
            }
        }
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
