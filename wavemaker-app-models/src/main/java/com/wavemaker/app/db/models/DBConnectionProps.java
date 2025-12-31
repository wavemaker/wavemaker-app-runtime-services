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
package com.wavemaker.app.db.models;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.wavemaker.app.db.constants.DataSourceType;
import com.wavemaker.app.db.util.DBUtils;
import com.wavemaker.app.security.models.annotation.FrameworkProfileProperty;
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.db.constants.DBType;

/**
 * @author Sunil Kumar
 * @author Dilip Kumar
 */
public class DBConnectionProps extends DBTestConnectionProps {

    public static final int DB_MAX_PAGE_SIZE = 100;
    private static final int DB_MIN_POOL_SIZE = 2;
    private static final int DB_MAX_POOL_SIZE = 4;
    private static final int TRANSACTION_TIMEOUT = 30;
    public static final String HBM_DDL_NONE = "none";

    private String serviceId;
    @NotBlank
    private String packageName;

    @NotNull
    private DBType dbType;

    private String host;
    @NotBlank
    private String dbName;
    private String port;
    @ProfilizableProperty("schemaName")
    private String schemaName;
    private List<TableSelector> tableFilter = Collections.emptyList();
    private List<String> schemaFilter = Collections.emptyList();
    @ProfilizableProperty("impersonateUser")
    private boolean impersonateUser;

    private String activeDirectoryDomain;

    @ProfilizableProperty("hbm2ddl")
    private String hbm2ddl;

    @FrameworkProfileProperty(value = "maxPageSize", defaultRef = "${db.maxPageSize}")
    private Integer maxPageSize;

    @FrameworkProfileProperty(value = "minPoolSize", defaultRef = "${db.minPoolSize}")
    private Integer minPoolSize;

    @FrameworkProfileProperty(value = "maxPoolSize", defaultRef = "${db.maxPoolSize}")
    private Integer maxPoolSize;

    @FrameworkProfileProperty(value = "transactionTimeout", defaultRef = "${db.transactionTimeout}")
    private Integer transactionTimeout;

    @ProfilizableProperty("dataSourceType")
    private DataSourceType dataSourceType;

    @ProfilizableProperty("jndiName")
    private String jndiName;

    private DataModelReloadInfo reloadInfo;
    private final boolean readOnly = true;
    private String metadataDialect;
    private String dbServerVersion;

    public DBConnectionProps() {
        this.hbm2ddl = HBM_DDL_NONE;
        this.maxPageSize = DB_MAX_PAGE_SIZE;
        this.minPoolSize = DB_MIN_POOL_SIZE;
        this.maxPoolSize = DB_MAX_POOL_SIZE;
        this.transactionTimeout = TRANSACTION_TIMEOUT;
        this.jndiName = StringUtils.EMPTY;
        this.dataSourceType = DataSourceType.WM_MANAGED_DATASOURCE;
    }

    public DBConnectionProps(final DBConnectionProps other) {
        super(other);
        this.serviceId = other.serviceId;
        this.packageName = other.packageName;
        this.dbType = other.dbType;
        this.host = other.host;
        this.dbName = other.dbName;
        this.port = other.port;
        this.schemaName = other.schemaName;
        this.tableFilter = other.tableFilter;
        this.schemaFilter = other.schemaFilter;
        this.impersonateUser = other.impersonateUser;
        this.activeDirectoryDomain = other.activeDirectoryDomain;
        this.hbm2ddl = other.hbm2ddl;
        this.maxPageSize = other.maxPageSize;
        this.minPoolSize = other.minPoolSize;
        this.maxPoolSize = other.maxPoolSize;
        this.transactionTimeout = other.transactionTimeout;
        this.dataSourceType = other.dataSourceType;
        this.jndiName = other.jndiName;
        this.reloadInfo = other.reloadInfo;
        //this.readOnly = other.readOnly;
        this.metadataDialect = other.metadataDialect;
        this.dbServerVersion = other.dbServerVersion;
    }

    public String getServiceId() {
        return serviceId;
    }

    public DBConnectionProps setServiceId(final String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public DBConnectionProps setPackageName(final String packageName) {
        this.packageName = packageName;
        return this;
    }

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(final DBType dbType) {
        this.dbType = (dbType != null) ? dbType : DBUtils.getDBTypeByUrl(getUrl());
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getDbName() {
        return dbName;
    }

    public DBConnectionProps setDbName(final String dbName) {
        this.dbName = dbName;
        return this;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public List<TableSelector> getTableFilter() {
        return tableFilter;
    }

    public DBConnectionProps setTableFilter(final List<TableSelector> tableFilter) {
        if (tableFilter != null) {
            this.tableFilter = tableFilter;
        }
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public DBConnectionProps setSchemaName(final String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public List<String> getSchemaFilter() {
        return schemaFilter;
    }

    public DBConnectionProps setSchemaFilter(final List<String> schemaFilter) {
        if (schemaFilter != null) {
            this.schemaFilter = schemaFilter;
        }
        return this;
    }

    public boolean getImpersonateUser() {
        return impersonateUser;
    }

    public DBConnectionProps setImpersonateUser(final boolean impersonateUser) {
        this.impersonateUser = impersonateUser;
        return this;
    }

    public String getActiveDirectoryDomain() {
        return activeDirectoryDomain;
    }

    public DBConnectionProps setActiveDirectoryDomain(final String activeDirectoryDomain) {
        this.activeDirectoryDomain = activeDirectoryDomain;
        return this;
    }

    public String getHbm2ddl() {
        return hbm2ddl;
    }

    public DBConnectionProps setHbm2ddl(String hbm2ddl) {
        this.hbm2ddl = hbm2ddl;
        return this;
    }

    public Integer getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public Integer getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(final Integer minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(final Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Integer getTransactionTimeout() {
        return transactionTimeout;
    }

    public DBConnectionProps setTransactionTimeout(final Integer transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
        return this;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        //this.readOnly = readOnly;
    }

    public DataModelReloadInfo getReloadInfo() {
        return reloadInfo;
    }

    public void setReloadInfo(final DataModelReloadInfo reloadInfo) {
        this.reloadInfo = reloadInfo;
    }

    public String getMetadataDialect() {
        return metadataDialect;
    }

    public void setMetadataDialect(String metadataDialect) {
        this.metadataDialect = metadataDialect;
    }

    public String getDbServerVersion() {
        return dbServerVersion;
    }

    public void setDbServerVersion(String dbServerVersion) {
        this.dbServerVersion = dbServerVersion;
    }

    @Override
    public DBConnectionProps clone() {
        return new DBConnectionProps(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBConnectionProps that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return getImpersonateUser() == that.getImpersonateUser() &&
            getMaxPageSize() == that.getMaxPageSize() &&
            getMinPoolSize() == that.getMinPoolSize() &&
            getMaxPageSize() == that.getMaxPoolSize() &&
            getTransactionTimeout() == that.getTransactionTimeout() &&
            Objects.equals(getServiceId(), that.getServiceId()) &&
            Objects.equals(getPackageName(), that.getPackageName()) &&
            getDbType() == that.getDbType() &&
            Objects.equals(getHost(), that.getHost()) &&
            Objects.equals(getDbName(), that.getDbName()) &&
            Objects.equals(getPort(), that.getPort()) &&
            Objects.equals(getSchemaName(), that.getSchemaName()) &&
            Objects.equals(getTableFilter(), that.getTableFilter()) &&
            Objects.equals(getSchemaFilter(), that.getSchemaFilter()) &&
            Objects.equals(getActiveDirectoryDomain(), that.getActiveDirectoryDomain()) &&
            Objects.equals(getHbm2ddl(), that.getHbm2ddl()) &&
            getDataSourceType() == that.getDataSourceType() &&
            Objects.equals(getJndiName(), that.getJndiName()) &&
            Objects.equals(getReloadInfo(), that.getReloadInfo()) &&
            Objects.equals(isReadOnly(), that.isReadOnly()) &&
            Objects.equals(getMetadataDialect(), that.getMetadataDialect()) &&
            Objects.equals(getDbServerVersion(), that.getDbServerVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getServiceId());
    }

    @Override
    public String toString() {
        return "DBConnectionProps{" +
            "serviceId='" + serviceId + '\'' +
            ", packageName='" + packageName + '\'' +
            ", dbType=" + dbType +
            ", host='" + host + '\'' +
            ", dbName='" + dbName + '\'' +
            ", port='" + port + '\'' +
            ", schemaName='" + schemaName + '\'' +
            ", tableFilter=" + tableFilter +
            ", schemaFilter=" + schemaFilter +
            ", impersonateUser=" + impersonateUser +
            ", activeDirectoryDomain='" + activeDirectoryDomain + '\'' +
            ", hbm2ddl='" + hbm2ddl + '\'' +
            ", maxPageSize=" + maxPageSize +
            ", minPoolSize=" + minPoolSize +
            ", maxPoolSize=" + maxPoolSize +
            ", transactionTimeout=" + transactionTimeout +
            ", dataSourceType=" + dataSourceType +
            ", jndiName='" + jndiName + '\'' +
            ", reloadInfo=" + reloadInfo +
            ", readOnly=" + readOnly +
            ", metadataDialect='" + metadataDialect + '\'' +
            ", dbServerVersion='" + dbServerVersion + '\'' +
            '}';
    }

}
