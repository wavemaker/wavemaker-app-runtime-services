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

package com.wavemaker.app.db.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wavemaker.studio.core.data.constants.DBType;

public class DBUtils {
    private static final Logger logger = LoggerFactory.getLogger(DBUtils.class);
    public static final String JDBC_URL_SCHEME = "jdbc:";

    public static DBType getDBTypeByUrl(String url) {
        if (!url.startsWith(JDBC_URL_SCHEME)) {
            logger.warn("The url [{}] did not start with {}", url, JDBC_URL_SCHEME);
            return DBType.OTHER;
        }

        String urlWithoutJdbc = url.substring(JDBC_URL_SCHEME.length()).toLowerCase();

        // MAV-1769
        if (urlWithoutJdbc.startsWith(JDBC_URL_SCHEME)) {
            return DBType.SQL_SERVER;
        }

        DBType[] dbTypes = DBType.values();
        for (DBType dbType : dbTypes) {
            List<String> supportedJdbcProtocols = dbType.getSupportedJdbcProtocols();
            for (String supportedJdbcProtocol : supportedJdbcProtocols) {
                if (urlWithoutJdbc.startsWith(supportedJdbcProtocol)) {
                    return dbType;
                }
            }
        }
        return DBType.OTHER;

    }
}
