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

import com.wavemaker.runtime.data.hql.SelectQueryBuilder;
import com.wavemaker.runtime.data.model.AggregationInfo;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 29/11/17
 */
public class QueryGeneratorDecorator<E, I> implements EntityQueryGenerator<E, I> {

    private final EntityQueryGenerator<E, I> delegate;

    public QueryGeneratorDecorator(final EntityQueryGenerator<E, I> delegate) {
        this.delegate = delegate;
    }

    @Override
    public SelectQueryBuilder findById(final I identifier) {
        return delegate.findById(identifier);
    }

    @Override
    public SelectQueryBuilder findBy(final Map<String, Object> fieldValueMap) {
        return delegate.findBy(fieldValueMap);
    }

    @Override
    public SelectQueryBuilder searchByQuery(final String query) {
        return delegate.searchByQuery(query);
    }

    @Override
    public SelectQueryBuilder getAggregatedValues(final AggregationInfo aggregationInfo) {
        return delegate.getAggregatedValues(aggregationInfo);
    }
}
