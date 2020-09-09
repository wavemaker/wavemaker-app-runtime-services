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
package com.wavemaker.runtime.data.hql;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.wavemaker.commons.util.Tuple;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.filter.WMQueryParamInfo;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 8/5/18
 */
public class UpdateQueryBuilder extends QueryBuilder<UpdateQueryBuilder> {

    private Map<String, Object> setters = new HashMap<>();

    public UpdateQueryBuilder(final Class<?> entityClass, boolean hqlSanitize) {
        super(entityClass, hqlSanitize);
    }

    public UpdateQueryBuilder withSetter(String fieldName, Object value) {
        this.setters.put(fieldName, value);
        return this;
    }

    public WMQueryInfo build() {
        Map<String, WMQueryParamInfo> parameters = new HashMap<>();
        String query = "update " +
                generateFromClause(parameters, true) +
                generateSetClause(parameters) +
                generateWhereClause(parameters);

        return new WMQueryInfo(query, parameters);
    }

    private String generateSetClause(Map<String, WMQueryParamInfo> parameters) {
        return (setters.entrySet().stream()
                .map(entry -> new Tuple.Two<>(entry, "wm_setter_" + entry.getKey()))
                .peek(tuple -> parameters.put(tuple.v2, new WMQueryParamInfo(tuple.v1.getValue())))
                .map(tuple -> tuple.v1.getKey() + " = :" + tuple.v2)
                .collect(Collectors.joining(", ", " set ", " ")));
    }

}
