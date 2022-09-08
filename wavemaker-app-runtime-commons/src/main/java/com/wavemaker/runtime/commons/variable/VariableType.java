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
package com.wavemaker.runtime.commons.variable;

import com.wavemaker.runtime.commons.WMAppContext;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 23/6/16
 */
public enum VariableType {
    PROMPT,
    SERVER(ServerVariableValueProvider.class),
    APP_ENVIRONMENT(AppEnvironmentVariableValueProvider.class);

    private boolean variable;
    private Class<? extends VariableValueProvider> variableValueProviderClass;
    private VariableValueProvider variableValueProvider;

    VariableType() {
    }

    VariableType(Class<? extends VariableValueProvider> variableValueProviderClass) {
        this.variable = true;
        this.variableValueProviderClass = variableValueProviderClass;
    }

    public boolean isVariable() {
        return variable;
    }

    public Object getValue(String variableName) {
        VariableValueProvider variableValueProvider = getVariableValueProvider();
        if (variableValueProvider != null) {
            return variableValueProvider.getValue(variableName);
        }
        return null;
    }

    public <T> T getValue(String variableName, Class<T> requiredType) {
        VariableValueProvider variableValueProvider = getVariableValueProvider();
        if (variableValueProvider != null) {
            return variableValueProvider.getValue(variableName, requiredType);
        }
        return null;
    }

    public String toVariableName(String variableName, String parameterName) {
        return VariableTypeHelper.toVariableName(this, variableName, parameterName);
    }

    private VariableValueProvider getVariableValueProvider() {
        if (variableValueProvider == null && variableValueProviderClass != null) {
            variableValueProvider = WMAppContext.getInstance().getSpringBean(variableValueProviderClass);
        }
        return variableValueProvider;
    }
}
