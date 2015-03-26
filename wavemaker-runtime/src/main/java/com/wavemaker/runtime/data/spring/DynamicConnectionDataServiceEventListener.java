/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.data.spring;

import com.wavemaker.runtime.data.DataServiceEventListener;
import com.wavemaker.runtime.service.ServiceWire;
import com.wavemaker.runtime.service.TypedServiceReturn;

/**
 * @author Simon Toens
 */
public class DynamicConnectionDataServiceEventListener extends DataServiceEventListener {

    private static int counter = 0;

    @Override
    public Object[] preOperation(ServiceWire serviceWire, String operationName, Object[] params) {

        ThreadLocalDriverManagerDataSource.ConnectionProperties props = new ThreadLocalDriverManagerDataSource.ConnectionProperties();
        if (counter++ % 2 == 0) {
            props.setUrl("jdbc:mysql://localhost:3306/test");
        } else {
            props.setUrl("jdbc:mysql://quad:3306/test");
        }

        return super.preOperation(serviceWire, operationName, params);
    }

    @Override
    public TypedServiceReturn postOperation(ServiceWire serviceWire, String operationName, TypedServiceReturn result, Throwable th) throws Throwable {

        ThreadLocalDriverManagerDataSource.unsetConnectionProperties();
        return super.postOperation(serviceWire, operationName, result, th);
    }

}
