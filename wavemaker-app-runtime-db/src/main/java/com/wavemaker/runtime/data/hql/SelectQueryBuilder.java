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
package com.wavemaker.runtime.data.hql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.filter.WMQueryParamInfo;
import com.wavemaker.runtime.data.model.Aggregation;
import com.wavemaker.runtime.data.model.AggregationInfo;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 8/5/18
 */
public class SelectQueryBuilder extends QueryBuilder<SelectQueryBuilder> {

    private List<String> fields;
    private String distinctField;

    private List<String> groupByFields;
    private List<Aggregation> aggregations;

    public SelectQueryBuilder(final Class<?> entityClass) {
        super(entityClass);
    }

    public static SelectQueryBuilder newBuilder(Class<?> entity) {
        return new SelectQueryBuilder(entity);
    }

    public SelectQueryBuilder withFields(final List<String> fields) {
        this.fields = fields;
        return this;
    }

    public SelectQueryBuilder withDistinctFields(final String distinctField) {
        this.distinctField = distinctField;
        return this;
    }

    public SelectQueryBuilder withGroupByFields(final List<String> groupByFields) {
        this.groupByFields = groupByFields;
        return this;
    }

    public SelectQueryBuilder withAggregations(final List<Aggregation> aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    public SelectQueryBuilder withAggregationInfo(AggregationInfo aggregationInfo) {
        withGroupByFields(aggregationInfo.getGroupByFields())
            .withAggregations(aggregationInfo.getAggregations())
            .withFilter(aggregationInfo.getFilter());
        return this;
    }

    public WMQueryInfo build() {
        StringBuilder builder = new StringBuilder();
        Map<String, WMQueryParamInfo> parameters = new HashMap<>();

        final String projections = generateProjections();

        if (StringUtils.isNotBlank(projections)) {
            builder.append("select ")
                .append(projections)
                .append(" ");
        }

        builder.append(generateFromClause(parameters, false));
        builder.append(generateWhereClause(parameters));

        if (CollectionUtils.isNotEmpty(groupByFields)) {
            builder.append("group by ")
                .append(StringUtils.join(groupByFields, ","))
                .append(" ");
        }

        return new WMQueryInfo(builder.toString(), parameters);
    }

    public Optional<WMQueryInfo> buildCountQuery() {
        Optional<WMQueryInfo> result = Optional.empty();

        // hql doesn't support group by in combination of group by
        if (CollectionUtils.isEmpty(groupByFields)) {
            Map<String, WMQueryParamInfo> parameters = new HashMap<>();

            final String countQuery = "select count(*) " +
                generateFromClause(parameters, false) +
                generateWhereClause(parameters);

            result = Optional.of(new WMQueryInfo(countQuery, parameters));

        }

        return result;
    }

    private String generateProjections() {
        List<String> projections = new ArrayList<>();

        if (StringUtils.isNotEmpty(distinctField)) {
            projections.add("distinct(" + distinctField + ") as " + cleanAlias(distinctField));
        }

        if (CollectionUtils.isNotEmpty(fields)) {
            projections.addAll(fields);
        }

        if (CollectionUtils.isNotEmpty(groupByFields)) {
            for (final String field : groupByFields) {
                projections.add(field + " as " + cleanAlias(field));
            }
        }

        if (CollectionUtils.isNotEmpty(aggregations)) {
            for (final Aggregation aggregation : aggregations) {
                projections.add(aggregation.asSelection());
            }
        }

        return StringUtils.join(projections, ",");
    }

    private String cleanAlias(String alias) {
        return alias.replaceAll("\\.", "\\$");
    }

}
