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
package com.wavemaker.runtime.data.dialect;

//import java.sql.Types;
//
//import javax.imageio.spi.ServiceRegistry;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.InnoDBStorageEngine;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.MySQLStorageEngine;
/*import org.hibernate.dialect.function.SqlFunction;
import org.hibernate.dialect.function.TrimFunction;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.type.BasicType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.hibernate.type.spi.TypeConfiguration;

import com.wavemaker.commons.CommonConstants;*/

/**
 * @author Simon Toens
 */
public class WMMySQLDialect extends MySQLDialect {

    public WMMySQLDialect() {
        super();
//        registerFunction("uuid", new NoArgSQLFunction("uuid", StringJavaType.INSTANCE));
//        //as hibernate timestamp is mapping to sql datetime in mysql,So forcing hibernate timestamp to map sql timestamp.
//        registerColumnType(Types.TIMESTAMP, "timestamp");
//        registerColumnType(CommonConstants.DATE_TIME_WM_TYPE_CODE, "datetime");
    }

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry().registerNoArgs("uuid");
//
//        TypeConfiguration typeConfiguration = queryEngine.getTypeConfiguration();
//        SqmFunctionRegistry functionRegistry = queryEngine.getSqmFunctionRegistry();
//        functionRegistry.registerPattern(
//            "uuid",
//            "uuid",
//            basicTypeRegistry.resolve(StandardBasicTypes.STRING));
    }

    @Override
    protected MySQLStorageEngine getDefaultMySQLStorageEngine() {
        return InnoDBStorageEngine.INSTANCE;
    }
}
