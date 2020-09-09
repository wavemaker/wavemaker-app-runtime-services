/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.model.procedures;

import javax.validation.constraints.NotNull;

import com.wavemaker.runtime.data.model.queries.QueryParameter;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 5/10/16
 */
public class ProcedureParameter extends QueryParameter {

    @NotNull
    private ProcedureParameterType parameterType;

    // Needed for jackson deserialization
    public ProcedureParameter() {
    }

    public ProcedureParameter(final ProcedureParameter other) {
        super(other);
        this.parameterType = other.parameterType;
    }

    public ProcedureParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(final ProcedureParameterType parameterType) {
        this.parameterType = parameterType;
    }
}
