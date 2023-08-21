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

import javax.validation.constraints.NotEmpty;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.PersistenceConfig;
import com.wavemaker.commons.util.SystemUtils;

public class MongoPersistenceConfig implements PersistenceConfig {

    public static final String MONGODB = "MONGODB";

    @NotEmpty
    @ProfilizableProperty("${security.session.mongodb.host}")
    private String host;

    @NotEmpty
    @ProfilizableProperty("${security.session.mongodb.port}")
    private String port;

    @NotEmpty
    @ProfilizableProperty("${security.session.mongodb.dbname}")
    private String dbname;

    @NotEmpty
    @ProfilizableProperty("${security.session.mongodb.username}")
    private String username;

    @NotEmpty
    @ProfilizableProperty("${security.session.mongodb.password}")
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = SystemUtils.encryptIfNotEncrypted(password);
    }

    @Override
    public String getType() {
        return MONGODB;
    }
}
