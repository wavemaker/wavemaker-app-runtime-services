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

package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.wavemaker.runtime.data.event.EntityPostFetchEvent;
import com.wavemaker.runtime.data.event.EntityPreFetchEvent;

class FindMethodInvocationHandler implements CRUDMethodInvocationHandler {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void preHandle(String serviceId, Class entityClass, Method method, java.lang.Object[] args) {
        Object entityId = args[0];
        applicationEventPublisher.publishEvent(new EntityPreFetchEvent<>(serviceId, entityClass, entityId));
    }

    @Override
    public void postHandle(String serviceId, Class entityClass, Method method, java.lang.Object retVal) {
        applicationEventPublisher.publishEvent(new EntityPostFetchEvent<>(serviceId, entityClass, retVal));
    }

    @Override
    public boolean matches(Class entityClass, Method method) {
        String methodName = method.getName();
        return ("getById".equals(methodName) || "findById".equals(methodName)) && method.getParameterTypes().length == 1;
    }
}
