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
package com.wavemaker.runtime.security;

import java.io.Serializable;

/**
 * Created by srujant on 14/11/18.
 */
public class Attribute implements Serializable{


    private AttributeScope scope;
    private Object value;

    public Attribute(AttributeScope scope, Object value) {
        this.scope = scope;
        this.value = value;
    }

    public AttributeScope getScope() {
        return scope;
    }

    public Object getValue() {
        return value;
    }


    public enum AttributeScope {
        /*
        *  This attributescoped variables will be visible to both client and server.
        * */
        ALL,
        /*
        * This attributescoped variables will be visible only to the server.
        * */
        SERVER_ONLY;
    }

}
