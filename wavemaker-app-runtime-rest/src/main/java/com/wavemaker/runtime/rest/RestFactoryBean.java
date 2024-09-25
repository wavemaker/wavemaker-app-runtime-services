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

package com.wavemaker.runtime.rest;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

import com.wavemaker.runtime.rest.service.RestRuntimeService;

public class RestFactoryBean<T> implements FactoryBean<T> {

    private Class<T> serviceKlass;

    private String serviceId;

    private ClassLoader classLoader;

    @Autowired
    private RestRuntimeService restRuntimeService;

    @Value("${app.rest.apiorchestration.encoding.mode:TEMPLATE_AND_VALUES}")
    private EncodingMode encodingMode;

    public RestFactoryBean(Class<T> serviceKlass, String serviceId, ClassLoader classLoader) {
        this.serviceKlass = serviceKlass;
        this.serviceId = serviceId;
        this.classLoader = classLoader;
    }

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(
            classLoader,
            new Class[]{serviceKlass}, new RestInvocationHandler(serviceId, restRuntimeService, encodingMode));
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceKlass;
    }
}
