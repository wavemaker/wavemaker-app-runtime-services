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

package com.wavemaker.runtime.service.reflect;

import java.lang.reflect.Method;

import com.wavemaker.runtime.service.ParsedServiceArguments;

/**
 * @author Matt Small
 */
public class ReflectParsedServiceArguments extends ParsedServiceArguments {

    public ReflectParsedServiceArguments() {

    }

    public ReflectParsedServiceArguments(ParsedServiceArguments psa) {
        this();
        this.setArguments(psa.getArguments());
        this.setGettersCalled(psa.getGettersCalled());
    }

    private Method method;

    private Object serviceObject;

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getServiceObject() {
        return this.serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }
}
