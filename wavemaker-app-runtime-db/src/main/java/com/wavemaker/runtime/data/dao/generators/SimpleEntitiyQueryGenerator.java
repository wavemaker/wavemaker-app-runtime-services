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
package com.wavemaker.runtime.data.dao.generators;

import java.util.Map;

import jakarta.persistence.IdClass;

import com.wavemaker.runtime.data.hql.SelectQueryBuilder;
import com.wavemaker.runtime.data.model.AggregationInfo;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 29/11/17
 */
public class SimpleEntitiyQueryGenerator<E, I> implements EntityQueryGenerator<E, I> {

    private final Class<E> entityClass;

    private final IdentifierStrategy<E, I> identifierStrategy;

    @SuppressWarnings("unchecked")
    public SimpleEntitiyQueryGenerator(final Class<E> entityClass) {
        this.entityClass = entityClass;

        if (entityClass.isAnnotationPresent(IdClass.class)) {
            final IdClass idClass = entityClass.getAnnotation(IdClass.class);
            identifierStrategy = new CompositeIdentifierStrategy<>((Class<I>) idClass.value());
        } else {
            identifierStrategy = new SingleIdentifierStrategy<>(entityClass);
        }
    }

    @Override
    public SelectQueryBuilder findById(final I identifier) {
        SelectQueryBuilder builder = SelectQueryBuilder.newBuilder(entityClass);

        builder.withFilterConditions(identifierStrategy.extract(identifier));

        return builder;
    }

    @Override
    public SelectQueryBuilder findBy(final Map<String, Object> fieldValueMap) {
        return SelectQueryBuilder.newBuilder(entityClass)
            .withFilterConditions(fieldValueMap);
    }

    @Override
    public SelectQueryBuilder searchByQuery(final String query) {
        return SelectQueryBuilder.newBuilder(entityClass)
            .withFilter(query);
    }

    @Override
    public SelectQueryBuilder getAggregatedValues(final AggregationInfo aggregationInfo) {
        return SelectQueryBuilder.newBuilder(entityClass)
            .withAggregationInfo(aggregationInfo);
    }
}
