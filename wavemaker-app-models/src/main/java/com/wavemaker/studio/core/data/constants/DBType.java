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

package com.wavemaker.studio.core.data.constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DBType {
    HSQL("HSQLDB", List.of("hsqldb"), "org/springframework/session/jdbc/schema-hsqldb.sql"),
    MYSQL("MySQL", List.of("mariadb", "mysql"), "org/springframework/session/jdbc/schema-mysql.sql"),
    POSTGRES("PostgreSQL", List.of("postgresql"), "org/springframework/session/jdbc/schema-postgresql.sql"),
    ORACLE("Oracle", List.of("oracle"), "org/springframework/session/jdbc/schema-oracle.sql"),
    SQL_SERVER("SQLServer", List.of("sqlserver"), "org/springframework/session/jdbc/schema-sqlserver.sql"),
    DB2("DB2", List.of("db2"), "org/springframework/session/jdbc/schema-db2.sql"),
    SQLITE("sql_lite", List.of("sqllite"), "org/springframework/session/jdbc/schema-sqllite.sql"),
    REDSHIFT("Redshift", List.of("redshift"), "org/springframework/session/jdbc/schema-redshift.sql"),
    SAP_HANA("SAP_HANA", List.of("sap"), "org/springframework/session/jdbc/schema-sap.sql"),
    OTHER("Other", List.of("other"), "");

    private static final Map<String, DBType> valueVsDBType = new HashMap<>();

    static {
        for (final DBType dbType : DBType.values()) {
            valueVsDBType.put(dbType.toValue(), dbType);
        }
    }

    @JsonCreator
    public static DBType fromValue(String value) {
        return valueVsDBType.get(value);
    }

    @JsonValue
    public String toValue() {
        return getId();
    }

    private final String id;
    private final List<String> supportedJdbcProtocols;
    private final String springSessionSchemaCreationFile;

    DBType(String id, List<String> supportedJdbcProtocols, String springSessionSchemaCreationFile) {
        this.id = id;
        this.supportedJdbcProtocols = supportedJdbcProtocols;
        this.springSessionSchemaCreationFile = springSessionSchemaCreationFile;
    }

    public String getId() {
        return id;
    }

    public List<String> getSupportedJdbcProtocols() {
        return supportedJdbcProtocols;
    }

    public String getSpringSessionSchemaCreationFile() {
        return springSessionSchemaCreationFile;
    }
}

