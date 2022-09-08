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

import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;

import com.wavemaker.runtime.data.dao.query.providers.ParametersProvider;
import com.wavemaker.runtime.data.dao.query.providers.QueryProvider;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 3/8/17
 */
public class QueryCallback<R> implements HibernateCallback<Optional<R>> {

    private final QueryProvider<R> queryProvider;
    private final ParametersProvider parametersProvider;

    public QueryCallback(
        final QueryProvider<R> queryProvider,
        final ParametersProvider parametersProvider) {
        this.queryProvider = queryProvider;
        this.parametersProvider = parametersProvider;
    }

    @Override
    public Optional<R> doInHibernate(final Session session) {
        final Query<R> query = queryProvider.getQuery(session, parametersProvider);
        return query.uniqueResultOptional();
    }
}
