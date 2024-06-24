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
package com.wavemaker.runtime.data.util;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.dialect.OracleTypes;

import com.wavemaker.commons.CommonConstants;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.data.model.JavaType;

public class JDBCUtils {

    private static final Map<JavaType, Integer> javaTypeVsSqlTypeCodes = new HashMap<>();

    private static Map<Integer, JavaType> sqlTypeCodeVsJavaType = new HashMap<>();

    static {
        javaTypeVsSqlTypeCodes.put(JavaType.BYTE, Types.TINYINT);
        javaTypeVsSqlTypeCodes.put(JavaType.SHORT, Types.SMALLINT);
        javaTypeVsSqlTypeCodes.put(JavaType.INTEGER, Types.INTEGER);
        javaTypeVsSqlTypeCodes.put(JavaType.LONG, Types.BIGINT);
        javaTypeVsSqlTypeCodes.put(JavaType.BIG_INTEGER, Types.BIGINT);
        javaTypeVsSqlTypeCodes.put(JavaType.FLOAT, Types.FLOAT);
        javaTypeVsSqlTypeCodes.put(JavaType.DOUBLE, Types.DOUBLE);
        javaTypeVsSqlTypeCodes.put(JavaType.BIG_DECIMAL, Types.DECIMAL);
        javaTypeVsSqlTypeCodes.put(JavaType.BOOLEAN, Types.BOOLEAN);
        javaTypeVsSqlTypeCodes.put(JavaType.YES_OR_NO, Types.CHAR);
        javaTypeVsSqlTypeCodes.put(JavaType.TRUE_OR_FALSE, Types.CHAR);
        javaTypeVsSqlTypeCodes.put(JavaType.CHARACTER, Types.CHAR);
        javaTypeVsSqlTypeCodes.put(JavaType.STRING, Types.VARCHAR);
        javaTypeVsSqlTypeCodes.put(JavaType.TEXT, Types.LONGVARCHAR);
        javaTypeVsSqlTypeCodes.put(JavaType.CLOB, Types.CLOB);
        javaTypeVsSqlTypeCodes.put(JavaType.BLOB, Types.BLOB);
        javaTypeVsSqlTypeCodes.put(JavaType.DATE, Types.DATE);
        javaTypeVsSqlTypeCodes.put(JavaType.TIME, Types.TIME);
        javaTypeVsSqlTypeCodes.put(JavaType.DATETIME, Types.TIMESTAMP); // XXX
        javaTypeVsSqlTypeCodes.put(JavaType.TIMESTAMP, Types.TIMESTAMP);

        sqlTypeCodeVsJavaType.put(Types.BIT, JavaType.BOOLEAN);
        sqlTypeCodeVsJavaType.put(Types.BOOLEAN, JavaType.BOOLEAN);
        sqlTypeCodeVsJavaType.put(Types.TINYINT, JavaType.SHORT);
        sqlTypeCodeVsJavaType.put(Types.SMALLINT, JavaType.SHORT);
        sqlTypeCodeVsJavaType.put(Types.INTEGER, JavaType.INTEGER);
        sqlTypeCodeVsJavaType.put(Types.BIGINT, JavaType.BIG_INTEGER);
        sqlTypeCodeVsJavaType.put(Types.FLOAT, JavaType.FLOAT);
        sqlTypeCodeVsJavaType.put(Types.REAL, JavaType.BIG_DECIMAL);
        sqlTypeCodeVsJavaType.put(Types.DOUBLE, JavaType.DOUBLE);
        sqlTypeCodeVsJavaType.put(Types.NUMERIC, JavaType.BIG_DECIMAL);
        sqlTypeCodeVsJavaType.put(Types.DECIMAL, JavaType.BIG_DECIMAL);
        sqlTypeCodeVsJavaType.put(Types.LONGVARCHAR, JavaType.TEXT);
        sqlTypeCodeVsJavaType.put(Types.LONGNVARCHAR, JavaType.TEXT);
        sqlTypeCodeVsJavaType.put(Types.VARCHAR, JavaType.STRING);
        sqlTypeCodeVsJavaType.put(Types.CHAR, JavaType.CHARACTER);

        sqlTypeCodeVsJavaType.put(Types.DATE, JavaType.DATE);
        sqlTypeCodeVsJavaType.put(Types.TIME, JavaType.TIME);
        sqlTypeCodeVsJavaType.put(Types.TIMESTAMP, JavaType.TIMESTAMP);

        sqlTypeCodeVsJavaType.put(CommonConstants.DATE_TIME_WM_TYPE_CODE, JavaType.DATETIME);

        sqlTypeCodeVsJavaType.put(Types.BINARY, JavaType.BLOB);
        sqlTypeCodeVsJavaType.put(Types.VARBINARY, JavaType.BLOB);
        sqlTypeCodeVsJavaType.put(Types.LONGVARBINARY, JavaType.BLOB);
        sqlTypeCodeVsJavaType.put(Types.BLOB, JavaType.BLOB);
        sqlTypeCodeVsJavaType.put(Types.CLOB, JavaType.CLOB);
        sqlTypeCodeVsJavaType.put(Types.NCHAR, JavaType.STRING);
        sqlTypeCodeVsJavaType.put(Types.NVARCHAR, JavaType.STRING);
        sqlTypeCodeVsJavaType.put(Types.NCLOB, JavaType.CLOB);

    }

    private JDBCUtils() {
    }

    public static int getSqlTypeCode(JavaType javaType) {
        Integer typeCode = javaTypeVsSqlTypeCodes.get(javaType);

        if (typeCode == null) {
            // for lazy loading. Oracle Cursor type.
            if (javaType == JavaType.CURSOR) {
                typeCode = OracleTypes.CURSOR;
            } else {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.sql.type.code.not.found"), javaType);
            }
        }

        return typeCode;
    }

    public static JavaType getJavaType(Integer typeCode) {
        return sqlTypeCodeVsJavaType.get(typeCode);
    }
}
