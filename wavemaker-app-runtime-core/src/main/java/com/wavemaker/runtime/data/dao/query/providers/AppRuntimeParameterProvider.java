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
package com.wavemaker.runtime.data.dao.query.providers;

import java.util.Map;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.TypeHelper;
import org.hibernate.type.Type;

import com.wavemaker.commons.util.Tuple;
import com.wavemaker.runtime.data.dao.query.types.ParameterTypeResolver;
import com.wavemaker.runtime.data.dao.query.types.RuntimeParameterTypeResolver;
import com.wavemaker.runtime.data.dao.query.types.wmql.WMQLTypeHelper;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.replacers.providers.VariableTypeHelper;
import com.wavemaker.runtime.commons.variable.VariableType;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 4/8/17
 */
public class AppRuntimeParameterProvider implements ParametersProvider {

    private final Map<String, Object> parameters;
    private final ParameterTypeResolver resolver;

    public AppRuntimeParameterProvider(final Map<String, Object> parameters, final ParameterTypeResolver resolver) {
        this.parameters = parameters;
        this.resolver = resolver;
    }

    public AppRuntimeParameterProvider(WMQueryInfo queryInfo, TypeHelper typeHelper, WMQLTypeHelper wmqlTypeHelper) {
        this(queryInfo.getParameterValueMap(wmqlTypeHelper), new RuntimeParameterTypeResolver(queryInfo.getParameters(), typeHelper, wmqlTypeHelper));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getValue(final Session session, final String name) {
        Object value = parameters.get(name);
        // looking for system variables, only for null values.
        if (value == null) {
            final Tuple.Two<VariableType, String> variableInfo = VariableTypeHelper.fromVariableName(name);
            if (variableInfo.v1.isVariable()) {
                final Optional<Type> type = getType(session, name);
                if (type.isPresent()) {
                    value = variableInfo.v1.getValue(variableInfo.v2, type.get().getReturnedClass());
                } else {
                    value = variableInfo.v1.getValue(variableInfo.v2);
                }
            }
        }
        return value;
    }

    @Override
    public Optional<Type> getType(final Session session, final String name) {
        return resolver.resolveType(name);
    }
}
