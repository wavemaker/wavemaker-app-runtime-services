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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.wavemaker.runtime.data.filter.LegacyQueryFilterInterceptor;
import com.wavemaker.runtime.data.filter.QueryInterceptor;
import com.wavemaker.runtime.data.filter.WMQueryGrammarInterceptor;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.filter.WMQueryParamInfo;
import com.wavemaker.runtime.data.periods.PeriodClause;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 15/3/17
 */
public abstract class QueryBuilder<T extends QueryBuilder> {

    private static final QueryInterceptor legacyQueryFilterInterceptor = new LegacyQueryFilterInterceptor();
    private static final QueryInterceptor wmQueryGrammarInterceptor = new WMQueryGrammarInterceptor();

    protected final Class<?> entityClass;
    private final List<QueryInterceptor> interceptors;
    private List<PeriodClause> periodClauses = new ArrayList<>(2);
    private Map<String, Object> filterConditions = new HashMap<>();
    private String filter;

    public QueryBuilder(final Class<?> entityClass) {
        this.entityClass = entityClass;
        this.interceptors = new ArrayList<>();

        interceptors.add(legacyQueryFilterInterceptor);
        interceptors.add(wmQueryGrammarInterceptor);

    }

    @SuppressWarnings("unchecked")
    public T withPeriodClauses(final List<PeriodClause> periodClauses) {
        this.periodClauses.addAll(periodClauses);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withPeriodClause(PeriodClause periodClause) {
        this.periodClauses.add(periodClause);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFilterConditions(final Map<String, Object> filterConditions) {
        this.filterConditions.putAll(filterConditions);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFilterCondition(String fieldName, Object value) {
        this.filterConditions.put(fieldName, value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFilter(final String filter) {
        this.filter = filter;
        return (T) this;
    }

    protected String generateFromClause(final Map<String, WMQueryParamInfo> parameters, boolean updateDelete) {
        final StringBuilder builder = new StringBuilder();

        if (!updateDelete) {
            builder.append("from ");
        }

        builder.append(entityClass.getCanonicalName())
                .append(" ");

        periodClauses.stream()
                .map(PeriodClause::asWMQueryClause)
                .forEach(queryInfo -> {
                    builder.append("for ");
                    if (updateDelete) {
                        builder.append("portion of ");
                    }
                    builder.append(queryInfo.getQuery()).append(" ");
                    parameters.putAll(queryInfo.getParameters());
                });
        return builder.toString();
    }

    protected String generateWhereClause(final Map<String, WMQueryParamInfo> parameters) {
        final StringBuilder builder = new StringBuilder();

        if (!filterConditions.isEmpty() || StringUtils.isNotBlank(filter)) {
            builder.append("where ");

            builder.append(filterConditions.entrySet().stream()
                    .map(entry -> ImmutablePair.of(entry, "wm_filter_" + entry.getKey()))
                    .peek(pair -> parameters.put(pair.getRight(), new WMQueryParamInfo(pair.getLeft().getValue())))
                    .map(pair -> pair.getLeft().getKey() + " = :" + pair.getRight())
                    .collect(Collectors.joining(" and ", " ", " ")));

            if (StringUtils.isNotBlank(filter)) {
                final WMQueryInfo queryInfo = interceptFilter(filter);

                if (!filterConditions.isEmpty()) {
                    builder.append("and ");
                }
                builder.append(queryInfo.getQuery())
                        .append(" ");

                parameters.putAll(queryInfo.getParameters());
            }
        }

        return builder.toString();
    }

    private WMQueryInfo interceptFilter(String filter) {
        WMQueryInfo queryInfo = new WMQueryInfo(filter);

        for (final QueryInterceptor interceptor : interceptors) {
            interceptor.intercept(queryInfo, entityClass);
        }

        return queryInfo;
    }
}
