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
package com.wavemaker.app.security.models.config.session;

import jakarta.validation.constraints.NotEmpty;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.PersistenceConfig;
import com.wavemaker.commons.util.SystemUtils;

public class RedisPersistenceConfig implements PersistenceConfig {

    public static final String REDIS = "REDIS";

    @NotEmpty
    @ProfilizableProperty("${security.session.redis.host}")
    public String hostName;

    @NotEmpty
    @ProfilizableProperty("${security.session.redis.port}")
    public String port;

    @NotEmpty
    @ProfilizableProperty("${security.session.redis.database}")
    public String database;

    @NotEmpty
    @ProfilizableProperty("${security.session.redis.password}")
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
        this.password = SystemUtils.encryptIfNotEncrypted(password);
    }

    @Override
    public String getType() {
        return REDIS;
    }
}
