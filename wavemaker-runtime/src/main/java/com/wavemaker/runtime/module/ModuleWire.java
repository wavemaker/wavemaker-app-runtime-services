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

package com.wavemaker.runtime.module;

/**
 * Provides wiring between module beans and their entry points (the extension point that they bind to), as well as
 * exposing other configuration settings.
 * 
 * @author Matt Small
 */
public class ModuleWire {

    private String extensionPoint;

    private Object bean;

    private String name;

    public ModuleWire() {
        // do nothin'
    }

    public ModuleWire(String extensionPoint, Object bean) {
        this();
        setExtensionPoint(extensionPoint);
        setBean(bean);
    }

    public String getExtensionPoint() {
        return this.extensionPoint;
    }

    public void setExtensionPoint(String extensionPoint) {
        this.extensionPoint = extensionPoint;
    }

    public Object getBean() {
        return this.bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}