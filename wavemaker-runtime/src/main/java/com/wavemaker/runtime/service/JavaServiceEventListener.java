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

package com.wavemaker.runtime.service;

import com.wavemaker.runtime.data.util.SystemUtils;
import com.wavemaker.runtime.service.events.ServiceEventListener;

/**
 * Remove Hibernate proxies.
 * 
 * @author Simon Toens
 */
public class JavaServiceEventListener implements ServiceEventListener {

    @Override
    public TypedServiceReturn postOperation(ServiceWire serviceWire, String operationName, TypedServiceReturn result, Throwable throwable)
        throws Throwable {

        if (throwable != null) {
            throw throwable;
        }

        SystemUtils.clientPrepare();

        return result;
    }

    @Override
    public Object[] preOperation(ServiceWire serviceWire, String operationName, Object[] params) {
        return params;
    }
}