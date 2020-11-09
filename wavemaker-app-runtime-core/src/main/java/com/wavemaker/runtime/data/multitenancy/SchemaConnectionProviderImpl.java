/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaConnectionProviderImpl implements MultiTenantConnectionProvider, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(SchemaConnectionProviderImpl.class);
    private DataSource dataSource;
    private String defaultSchema;
    private TenantSchemaMappingResolver tenantSchemaMappingResolver;
    private SchemaSwitchResolverByDBType schemaSwitchResolver;

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public void setTenantSchemaMappingResolver(TenantSchemaMappingResolver tenantSchemaMappingResolver) {
        this.tenantSchemaMappingResolver = tenantSchemaMappingResolver;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setSchemaSwitchResolver(SchemaSwitchResolverByDBType schemaSwitchResolver) {
        this.schemaSwitchResolver = schemaSwitchResolver;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantId) throws SQLException {
        final Connection connection = getAnyConnection();
        if (!tenantId.equals(CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT)) {
            String schemaName = tenantSchemaMappingResolver.getSchemaName(tenantId);
            return schemaSwitchResolver.switchSchema(connection, schemaName);
        }
        return schemaSwitchResolver.switchSchema(connection, defaultSchema);
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
