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
package com.wavemaker.runtime.security.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.data.datasource.WMDataSource;

public class JdbcSessionScriptInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSessionScriptInitializer.class);

    @Resource(name = "${session.jdbc.serviceName}DataSource")
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
                ClassPathResource classPathResource =
                        new ClassPathResource("org/springframework/session/jdbc/schema-" + extractDbType(dataSource.getConnection().getMetaData().getURL()) + ".sql");
                LOGGER.debug("executing sql script from resource: {}", classPathResource.getPath());
                resourceDatabasePopulator.addScript(classPathResource);
                resourceDatabasePopulator.execute(dataSource);
            }
        } catch (SQLException throwables) {
            throw new WMRuntimeException(throwables.getCause());
        }
    }

    private String extractDbType(String url) {
        String[] connectionUrl = url.split(":");
        return connectionUrl[1];
    }
}
