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

package com.wavemaker.runtime.data;

import com.wavemaker.common.util.ObjectUtils;
import com.wavemaker.runtime.data.util.SystemUtils;
import com.wavemaker.runtime.service.ServiceWire;
import com.wavemaker.runtime.service.TypedServiceReturn;
import com.wavemaker.runtime.service.events.ServiceEventListener;

/**
 * @author Simon Toens
 */
public class DataServiceEventListener implements ServiceEventListener {

    @Override
    public Object[] preOperation(ServiceWire serviceWire, String operationName, Object[] params) {

        if (DataServiceLoggers.eventLogger.isInfoEnabled()) {
            DataServiceLoggers.eventLogger.info("startOperation " + operationName);
        }

        if (DataServiceLoggers.eventLogger.isDebugEnabled()) {
            if (params.length == 0) {
                DataServiceLoggers.eventLogger.debug("No input");
            } else {
                DataServiceLoggers.eventLogger.debug("Input " + ObjectUtils.objectToStringRecursive(params[0]));
            }
        }

        ThreadContext.setPreProcessorTask(DefaultTaskManager.getInstance().getPreProcessorRouterTask());

        return params;
    }

    @Override
    public TypedServiceReturn postOperation(ServiceWire serviceWire, String operationName, TypedServiceReturn result, Throwable th) throws Throwable {

        cleanup();

        if (th != null) {
            if (DataServiceLoggers.eventLogger.isInfoEnabled()) {
                DataServiceLoggers.eventLogger.info("failedOperation " + operationName + ": " + th);
            }

            throw th;
        }

        SystemUtils.clientPrepare();

        if (DataServiceLoggers.eventLogger.isInfoEnabled()) {
            DataServiceLoggers.eventLogger.info("postOperation " + operationName);
        }

        return result;
    }

    private void cleanup() {
        ThreadContext.unsetPreProcessorTask();
    }
}
