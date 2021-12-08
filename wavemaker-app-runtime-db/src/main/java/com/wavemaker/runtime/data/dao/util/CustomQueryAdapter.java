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
package com.wavemaker.runtime.data.dao.util;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.wavemaker.runtime.data.model.CustomQuery;
import com.wavemaker.runtime.data.model.CustomQueryParam;
import com.wavemaker.runtime.data.model.JavaType;
import com.wavemaker.runtime.data.model.queries.QueryParameter;
import com.wavemaker.runtime.data.model.queries.QueryType;
import com.wavemaker.runtime.data.model.queries.RuntimeQuery;
import com.wavemaker.runtime.data.util.JavaTypeUtils;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 4/8/17
 */
public class CustomQueryAdapter {

    private CustomQueryAdapter(){}

    private static Pattern QUERY_TYPE_PATTERN = Pattern.compile("^([^\\s]+)");

    public static RuntimeQuery adapt(CustomQuery query) {
        final List<QueryParameter> parameters = query.getQueryParams().stream()
                .map(CustomQueryAdapter::adapt)
                .collect(Collectors.toList());

        return new RuntimeQuery(query.getQueryStr(), query.isNativeSql(), findQueryType(query), parameters);
    }

    public static QueryParameter adapt(CustomQueryParam param) {
        final Optional<JavaType> javaType = JavaTypeUtils.fromClassName(param.getParamType());
        return new QueryParameter(param.getParamName(), javaType.orElseThrow(() ->
                new IllegalArgumentException(
                        "Unknown parameter type found:" + param.getParamType() + ", for parameter:" + param
                                .getParamName())),
                param.isList(), param.getParamValue());
    }

    private static QueryType findQueryType(CustomQuery query) {
        final String sqlScript = query.getQueryStr();
        QueryType queryType = QueryType.UPDATE;
        Matcher matcher = QUERY_TYPE_PATTERN.matcher(sqlScript);
        if (matcher.find()) {
            String prefix = matcher.group(1);
            try {
                if (prefix.equalsIgnoreCase("from")) {
                    queryType = QueryType.SELECT;
                } else {
                    queryType = QueryType.valueOf(prefix.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        return queryType;
    }
}
