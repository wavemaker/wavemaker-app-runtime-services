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
package com.wavemaker.runtime.data.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wavemaker.runtime.data.periods.PeriodClause;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 3/1/18
 */
public interface WMGenericTemporalDao<E, I> extends WMGenericDao<E, I> {

    Page<E> findByPeriod(List<PeriodClause> periodClauses, String query, Pageable pageable);

    Page<E> findByIdAndPeriod(Map<String, Object> identifier, List<PeriodClause> periodClauses, Pageable pageable);

    int update(Map<String, Object> identifier, PeriodClause periodClause, E entity);

    int update(PeriodClause periodClause, String filter, final E entity);

    int delete(Map<String, Object> identifier, PeriodClause periodClause);

    int delete(PeriodClause periodClause, String filter);
}
