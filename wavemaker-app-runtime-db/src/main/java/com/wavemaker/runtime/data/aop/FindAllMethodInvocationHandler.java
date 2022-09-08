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
import org.springframework.data.domain.Page;

import com.wavemaker.runtime.data.event.EntityPostFetchListEvent;
import com.wavemaker.runtime.data.event.EntityPreFetchListEvent;
import com.wavemaker.runtime.data.model.FetchQuery;

class FindAllMethodInvocationHandler implements CRUDMethodInvocationHandler {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void preHandle(String serviceId, Class entityClass, Method method, Object[] args) {
        String query = (String) args[0];
        FetchQuery fetchQuery = new FetchQuery(query);
        applicationEventPublisher.publishEvent(new EntityPreFetchListEvent<>(serviceId, entityClass, fetchQuery));
        args[0] = fetchQuery.getQuery();
    }

    @Override
    public void postHandle(String serviceId, Class entityClass, Method method, Object retVal) {
        applicationEventPublisher.publishEvent(new EntityPostFetchListEvent<>(serviceId, entityClass, (Page) retVal));
    }

    @Override
    public boolean matches(Class entityClass, Method method) {
        return "findAll".equals(method.getName()) && method.getParameterTypes().length >= 1 && method.getParameterTypes()[0] == String.class;
    }
}
