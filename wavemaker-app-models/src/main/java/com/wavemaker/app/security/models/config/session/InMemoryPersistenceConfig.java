/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.session;

import com.wavemaker.app.security.models.config.PersistenceConfig;

public class InMemoryPersistenceConfig implements PersistenceConfig {
    public static final String IN_MEMORY = "IN_MEMORY";

    @Override
    public String getType() {
        return IN_MEMORY;
    }
}
