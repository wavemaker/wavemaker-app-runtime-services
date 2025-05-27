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
package com.wavemaker.runtime.security.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.studio.core.data.constants.DBType;
import com.wavemaker.runtime.data.datasource.WMDataSource;

public class JdbcSessionScriptInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSessionScriptInitializer.class);

    @Resource(name = "${security.session.jdbc.serviceName}DataSource")
    private WMDataSource dataSource;

    @PostConstruct
    public void initializeScript() {
        try {
            ResultSet resultSet;
            String tableName = "SPRING_SESSION";
            try (Connection connection = dataSource.getTargetDataSource().getConnection()) {
                if ("PostgreSQL".equals(connection.getMetaData().getDatabaseProductName())) {
                    //PostgreSQL is storing tableNames in lower case so getTables check also need to be done on the lowercase
                    tableName = StringUtils.lowerCase(tableName);
                }
                resultSet = connection.getMetaData().getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"});
            }
            if (resultSet.next()) {
                LOGGER.debug("Skipping SPRING_SESSION table creation as it already exists");
            } else {
                ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
                ClassPathResource classPathResource = new ClassPathResource(getSchemaFileFromJdbcUrl(dataSource.getConnection().getMetaData().getURL()));
                LOGGER.debug("executing sql script from resource: {}", classPathResource.getPath());
                resourceDatabasePopulator.addScript(classPathResource);
                resourceDatabasePopulator.execute(dataSource);
            }
        } catch (SQLException throwables) {
            throw new WMRuntimeException(throwables.getCause());
        }
    }

    private String getSchemaFileFromJdbcUrl(String url) {
        String[] connectionUrl = url.split(":");
        String jdbcProtocol = connectionUrl[1];
        DBType[] dbTypes = DBType.values();
        for (DBType dbType : dbTypes) {
            List<String> supportedJdbcProtocols = dbType.getSupportedJdbcProtocols();
            for (String supportedJdbcProtocol : supportedJdbcProtocols) {
                if (Objects.equals(jdbcProtocol, supportedJdbcProtocol)) {
                    return dbType.getSpringSessionSchemaCreationFile();
                }
            }
        }
        throw new WMRuntimeException("Failed to retrieve DBType from URL");
    }
}
