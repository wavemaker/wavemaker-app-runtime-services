/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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
package com.wavemaker.studio.core.props;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * @author sunilp
 * @author Dilip Kumar
 */
public class DBTestConnectionProps implements Serializable, Cloneable {

    @ProfilizableProperty("username")
    private String username;

    @ProfilizableProperty("password")
    private String password;

    @NotBlank
    @ProfilizableProperty("url")
    private String url;

    @NotBlank
    @ProfilizableProperty("driverClass")
    private String driverClass;

    @NotBlank
    @ProfilizableProperty("dialect")
    private String dialect;

    public DBTestConnectionProps() {
    }

    public DBTestConnectionProps(final DBTestConnectionProps other) {
        this.username = other.username;
        this.password = other.password;
        this.url = other.url;
        this.driverClass = other.driverClass;
        this.dialect = other.dialect;
    }

    public String getUsername() {
        return username;
    }

    public DBTestConnectionProps setUsername(final String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DBTestConnectionProps setPassword(final String password) {
        this.password = password;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DBTestConnectionProps setUrl(final String url) {
        this.url = url;
        return this;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public DBTestConnectionProps setDriverClass(final String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    public String getDialect() {
        return dialect;
    }

    public DBTestConnectionProps setDialect(final String dialect) {
        this.dialect = dialect;
        return this;
    }

    @Override
    public DBTestConnectionProps clone() {
        return new DBTestConnectionProps(this);
    }

    @Override
    public String toString() {
        return "DBTestConnectionProps{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", url='" + url + '\'' +
            ", driverClass='" + driverClass + '\'' +
            ", dialect='" + dialect + '\'' +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBTestConnectionProps that)) {
            return false;
        }
        return Objects.equals(username, that.username) &&
            Objects.equals(password, that.password) &&
            Objects.equals(url, that.url) &&
            Objects.equals(driverClass, that.driverClass) &&
            Objects.equals(dialect, that.dialect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, url, driverClass, dialect);
    }
}
