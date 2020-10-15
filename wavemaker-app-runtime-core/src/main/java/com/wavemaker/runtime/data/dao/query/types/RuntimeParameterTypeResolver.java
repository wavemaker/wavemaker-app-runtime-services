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
package com.wavemaker.runtime.data.dao.query.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.TypeHelper;
import org.hibernate.type.Type;

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

    public RuntimeParameterTypeResolver(List<QueryParameter> parameters, TypeHelper typeHelper) {
        typesMap = parameters.stream()
                .collect(Collectors.toMap(QueryParameter::getName,
                        queryParameter -> typeHelper.heuristicType(queryParameter.getType().getClassName())));
    }

    public RuntimeParameterTypeResolver(Map<String, WMQueryParamInfo> parameters, TypeHelper typeHelper, WMQLTypeHelper wmqlTypeHelper) {
        typesMap = new HashMap<>();
        for (Map.Entry<String, WMQueryParamInfo> queryParamInfoEntry : parameters.entrySet()) {
            String key = queryParamInfoEntry.getKey();
            JavaType javaType = queryParamInfoEntry.getValue().getJavaType();

            if (javaType != null) {
                if (wmqlTypeHelper != null) {
                    javaType = wmqlTypeHelper.aliasFor(javaType);
                }
                typesMap.put(key, typeHelper.heuristicType(javaType.getClassName()));
            }

        }
    }

    public static RuntimeParameterTypeResolver from(Session session, RuntimeQuery query) {
        return new RuntimeParameterTypeResolver(query.getParameters(), session.getSessionFactory().getTypeHelper());
    }

    @Override
    public Optional<Type> resolveType(final String name) {
        return Optional.ofNullable(typesMap.get(name));
    }
}
