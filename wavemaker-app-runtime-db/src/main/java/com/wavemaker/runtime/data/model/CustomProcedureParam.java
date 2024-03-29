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

import com.wavemaker.runtime.data.model.procedures.ProcedureParameterType;

public class CustomProcedureParam {

    private Object paramValue;
    private String paramName;
    private ProcedureParameterType procedureParamType;
    private String valueType;

    public CustomProcedureParam() {
        super();
    }

    public CustomProcedureParam(
        String paramName, Object paramValue, ProcedureParameterType procedureParamType, String valueType) {
        this.procedureParamType = procedureParamType;
        this.paramName = paramName;
        this.valueType = valueType;
        this.paramValue = paramValue;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public ProcedureParameterType getProcedureParamType() {
        return procedureParamType;
    }

    public void setProcedureParamType(ProcedureParameterType procedureParamType) {
        this.procedureParamType = procedureParamType;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public void setParamValue(Object paramValue) {
        this.paramValue = paramValue;
    }

    public Object getParamValue() {
        return this.paramValue;
    }

    @Override
    public String toString() {
        return "CustomQueryParam [paramName=" + getParamName() + ",  paramValue=" + paramValue + "]";
    }

}
