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
package com.wavemaker.runtime.data.dao.query.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.spi.TypeConfiguration;

import com.wavemaker.runtime.data.dao.query.types.wmql.WMQLTypeHelper;
import com.wavemaker.runtime.data.filter.WMQueryParamInfo;
import com.wavemaker.runtime.data.model.JavaType;
import com.wavemaker.runtime.data.model.queries.QueryParameter;
import com.wavemaker.runtime.data.model.queries.RuntimeQuery;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 21/7/17
 */
public class RuntimeParameterTypeResolver implements ParameterTypeResolver {

    private final Map<String, Type> typesMap;

    public RuntimeParameterTypeResolver(List<QueryParameter> parameters, TypeConfiguration typeConfiguration) {
        typesMap = parameters.stream()
            .collect(Collectors.toMap(QueryParameter::getName,
                queryParameter -> typeConfiguration.getBasicTypeRegistry().getRegisteredType(queryParameter.getType().getClassName())
                    .getExpressibleJavaType().getJavaType()));
    }

    public RuntimeParameterTypeResolver(Map<String, WMQueryParamInfo> parameters, TypeConfiguration typeConfiguration, WMQLTypeHelper wmqlTypeHelper) {
        typesMap = new HashMap<>();
        for (Map.Entry<String, WMQueryParamInfo> queryParamInfoEntry : parameters.entrySet()) {
            String key = queryParamInfoEntry.getKey();
            JavaType javaType = queryParamInfoEntry.getValue().getJavaType();

            if (javaType != null) {
                if (wmqlTypeHelper != null) {
                    javaType = wmqlTypeHelper.aliasFor(javaType);
                }
                Type type = typeConfiguration.getBasicTypeRegistry().getRegisteredType(javaType.getClassName()).getExpressibleJavaType().getJavaType();
                typesMap.put(key, type);
            }

        }
    }

    public static RuntimeParameterTypeResolver from(Session session, RuntimeQuery query) {
        return new RuntimeParameterTypeResolver(query.getParameters(), ((SessionFactoryImplementor) session.getSessionFactory()).getTypeConfiguration());
    }

    @Override
    public Optional<Type> resolveType(final String name) {
        return Optional.ofNullable(typesMap.get(name));
    }
}
