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
package com.wavemaker.runtime.data.model;

import java.util.Map;

/**
 * @author Dilip Kumar
 * @since 1/6/18
 */
public class QueryProcedureInput<T> {

    private final String name;
    private final Map<String, Object> parameters;
    private final Class<T> responseType;

    public QueryProcedureInput(final String name, final Map<String, Object> parameters, final Class<T> responseType) {
        this.name = name;
        this.parameters = parameters;
        this.responseType = responseType;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Class<T> getResponseType() {
        return responseType;
    }
}
