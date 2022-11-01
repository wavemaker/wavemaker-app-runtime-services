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

package com.wavemaker.runtime.data.constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DBType {
    HSQL(List.of("hsqldb"), "hsqldb"),
    MYSQL(List.of("mariadb", "mysql"), "mysql"),
    POSTGRES(List.of("postgresql"), "postgresql"),
    ORACLE(List.of("oracle"), "oracle"),
    SQL_SERVER(List.of("sqlserver"), "sqlserver"),
    DB2(List.of("db2"), "db2"),
    SQLITE(List.of("sqllite"), "sqllite"),
    REDSHIFT(List.of("redshift"), "redshift"),
    SAP_HANA(List.of("sap"), "sap"),
    OTHER(List.of("other"), "other");

    private static final Map<String, DBType> valueVsDBType = new HashMap<>();

    static {
        for (final DBType dbType : DBType.values()) {
            valueVsDBType.put(dbType.toValue(), dbType);
        }
    }

    @JsonValue
    public String toValue() {
        return getSpringSessionSchemaType();
    }

    private String springSessionSchemaType;
    private List<String> supportedJdbcProtocols;

    DBType(List<String> supportedJdbcProtocols, String springSessionSchemaType) {
        this.supportedJdbcProtocols = supportedJdbcProtocols;
        this.springSessionSchemaType = springSessionSchemaType;
    }

    public List<String> getSupportedJdbcProtocols() {
        return supportedJdbcProtocols;
    }

    public String getSpringSessionSchemaType() {
        return springSessionSchemaType;
    }
}

