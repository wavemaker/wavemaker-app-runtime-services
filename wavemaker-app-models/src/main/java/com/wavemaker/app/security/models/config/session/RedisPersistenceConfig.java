/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.session;

import javax.validation.constraints.NotEmpty;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.PersistenceConfig;

public class RedisPersistenceConfig implements PersistenceConfig {

    public static final String REDIS = "REDIS";

    @NotEmpty
    @ProfilizableProperty(value = "${security.session.redis.host}", isAutoUpdate = true)
    public String hostName;

    @NotEmpty
    @ProfilizableProperty(value = "${security.session.redis.port}", isAutoUpdate = true)
    public String port;

    @NotEmpty
    @ProfilizableProperty(value = "${security.session.redis.database}", isAutoUpdate = true)
    public String database;

    @NotEmpty
    @ProfilizableProperty(value = "${security.session.redis.password}", isAutoUpdate = true)
    public String password;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getType() {
        return REDIS;
    }
}
