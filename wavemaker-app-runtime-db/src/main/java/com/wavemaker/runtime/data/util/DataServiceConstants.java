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

/**
 * @author Simon Toens
 */
public class DataServiceConstants {

    public static final String DATA_PACKAGE_NAME = "data";

    public static final String WEB_ROOT_TOKEN = "{WebAppRoot}";

    public static final String WM_MY_SQL_CLOUD_HOST_TOKEN = "{WM_CLOUD_MYSQL_HOST}";

    public static final String WM_MY_SQL_CLOUD_USER_NAME_TOKEN = "{WM_CLOUD_MYSQL_USER_NAME}";

    public static final String WM_MY_SQL_CLOUD_PASSWORD_TOKEN = "{WM_CLOUD_MYSQL_PASSWORD}";

    public static final String WM_MY_SQL_CLOUD_HOST;

    public static final String WM_MY_SQL_CLOUD_USER_NAME;

    public static final String WM_MY_SQL_CLOUD_PASSWORD;

    public static final String QUERY_EXECUTION_CONTROLLER = "QueryExecutionController";

    public static final String PROCEDURE_EXECUTION_CONTROLLER = "ProcedureExecutionController";

    static {
        final String mySqlCloudHost = "wm.mysqlCloudHost";
        final String mySqlCloudUsername = "wm.mysqlCloudUsername";
        final String mySqlCloudPassword = "wm.mysqlCloudPassword";
        WM_MY_SQL_CLOUD_HOST = (System.getenv(mySqlCloudHost) != null) ? System.getenv(mySqlCloudHost) : System.getProperty(mySqlCloudHost, "localhost:3306");
        WM_MY_SQL_CLOUD_USER_NAME = (System.getenv(mySqlCloudUsername) != null) ? System.getenv(mySqlCloudUsername) : System.getProperty(mySqlCloudUsername, "root");
        WM_MY_SQL_CLOUD_PASSWORD = (System.getenv(mySqlCloudPassword) != null) ? System.getenv(mySqlCloudPassword) : System.getProperty(mySqlCloudPassword, "cloudjee123");
    }

    private DataServiceConstants() {
    }

}
