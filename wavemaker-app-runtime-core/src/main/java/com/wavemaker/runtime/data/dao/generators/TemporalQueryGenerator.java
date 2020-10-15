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
package com.wavemaker.runtime.data.dao.generators;

import java.sql.Timestamp;

import com.wavemaker.runtime.data.annotations.TableTemporal;
import com.wavemaker.runtime.data.hql.SelectQueryBuilder;
import com.wavemaker.runtime.data.model.AggregationInfo;
import com.wavemaker.runtime.data.periods.AsOfClause;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 30/11/17
 */
public class TemporalQueryGenerator<E, I> extends QueryGeneratorDecorator<E, I> {

    private final TableTemporal.TemporalType type;

    public TemporalQueryGenerator(
            final EntityQueryGenerator<E, I> delegate,
            final TableTemporal.TemporalType type) {
        super(delegate);
        this.type = type;
    }

    @Override
    public SelectQueryBuilder searchByQuery(final String query) {
        return super.searchByQuery(query)
                .withPeriodClause(new AsOfClause(type, new Timestamp(System.currentTimeMillis())));
    }

    @Override
    public SelectQueryBuilder getAggregatedValues(
            final AggregationInfo aggregationInfo) {
        return super.getAggregatedValues(aggregationInfo)
                .withPeriodClause(new AsOfClause(type, new Timestamp(System.currentTimeMillis())));
    }
}
