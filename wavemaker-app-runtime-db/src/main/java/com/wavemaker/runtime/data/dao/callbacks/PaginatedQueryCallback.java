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
package com.wavemaker.runtime.data.dao.callbacks;

import java.util.List;
import java.util.Optional;

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.hibernate5.HibernateCallback;

import com.wavemaker.runtime.data.dao.query.providers.PaginatedQueryProvider;
import com.wavemaker.runtime.data.dao.query.providers.ParametersProvider;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 3/8/17
 */
public class PaginatedQueryCallback<R> implements HibernateCallback<Page<R>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaginatedQueryCallback.class);
    private final PaginatedQueryProvider<R> queryProvider;
    private final ParametersProvider parametersProvider;
    private final Pageable pageable;

    public PaginatedQueryCallback(
            final PaginatedQueryProvider<R> queryProvider, final ParametersProvider parametersProvider,
            final Pageable pageable) {
        this.queryProvider = queryProvider;
        this.parametersProvider = parametersProvider;
        this.pageable = pageable;
    }

    @Override
    public Page<R> doInHibernate(final Session session) {
        final Optional<Query<Number>> countQuery = queryProvider.getCountQuery(session, parametersProvider);
        long count = Integer.MAX_VALUE;
        try {
            Optional<Number> countOptional = countQuery.flatMap(Query::uniqueResultOptional);
            if (countOptional.isPresent()) {
                count = countOptional.get().longValue();
            }
        } catch (PersistenceException e) {
            LOGGER.warn("Count query execution failed, returning max value ERROR: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.warn("Count query execution failed, returning max value", e);
        }
        final Query<R> selectQuery = queryProvider.getQuery(session, pageable, parametersProvider);
        final List<R> result = selectQuery.list();

        return new PageImpl<>(result, pageable, count);
    }
}
