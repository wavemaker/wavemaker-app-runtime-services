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
package com.wavemaker.runtime.data.dao.query.providers;

import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 1/8/17
 */
public interface PaginatedQueryProvider<R> {

    /**
     * Returns or creates query from underlying data source with given pagination info.
     *
     * @param session  active hibernate session.
     * @param pageable pagination info
     *
     * @return query
     */
    Query<R> getQuery(Session session, Pageable pageable);

    /**
     * Returns count query to fetch the count from the underlying data source.
     *
     * @param session active hibernate session
     *
     * @return count query.
     */
    Optional<Query<Number>> getCountQuery(Session session);

    default Query<R> getQuery(Session session, Pageable pageable, ParametersProvider provider) {
        return provider.configure(session, getQuery(session, pageable));
    }

    default Optional<Query<Number>> getCountQuery(Session session, ParametersProvider provider) {
        final Optional<Query<Number>> queryOptional = getCountQuery(session);

        queryOptional.ifPresent(query -> provider.configure(session, query));

        return queryOptional;
    }
}
