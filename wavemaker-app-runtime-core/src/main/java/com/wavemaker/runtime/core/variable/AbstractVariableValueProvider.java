package com.wavemaker.runtime.core.variable;

import com.wavemaker.runtime.commons.variable.VariableValueProvider;
import com.wavemaker.runtime.data.util.JavaTypeUtils;

public abstract class AbstractVariableValueProvider implements VariableValueProvider {

    @Override
    public <T> T getValue(String variableName, Class<T> requiredType) {
        return (T) JavaTypeUtils.convert(requiredType.getCanonicalName(), getValue(variableName));
    }
}