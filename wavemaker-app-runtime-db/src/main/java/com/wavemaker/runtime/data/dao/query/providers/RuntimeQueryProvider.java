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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.Query;
import org.springframework.data.domain.Pageable;

import com.wavemaker.runtime.data.dao.util.QueryHelper;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.model.queries.RuntimeQuery;
import com.wavemaker.runtime.data.transform.Transformers;
import com.wavemaker.runtime.data.transform.WMResultTransformer;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 4/8/17
 */
public class RuntimeQueryProvider<R> implements QueryProvider<R>, PaginatedQueryProvider<R> {

    private final String queryString;
    private final String countQueryString;

    private final boolean nativeSql;

    private final Class<R> responseType;

    private RuntimeQueryProvider(final Builder<R> builder) {
        queryString = builder.queryString;
        countQueryString = builder.countQueryString;
        nativeSql = builder.nativeSql;
        responseType = builder.responseType;
    }

    public static <R> RuntimeQueryProvider<R> from(RuntimeQuery query, Class<R> returnType) {
        String countQueryString = query.getCountQueryString();

        if (StringUtils.isBlank(countQueryString)) {
            countQueryString = QueryHelper.getCountQuery(query.getQueryString(), query.isNativeSql());
        }

        return RuntimeQueryProvider.newBuilder(returnType)
            .withQueryString(query.getQueryString())
            .withCountQueryString(countQueryString)
            .withNativeSql(query.isNativeSql())
            .build();
    }

    public static <R> RuntimeQueryProvider<R> from(WMQueryInfo queryInfo, Class<R> returnType) {
        String countQuery = QueryHelper.getCountQuery(queryInfo.getQuery(), false);

        return RuntimeQueryProvider.newBuilder(returnType)
            .withQueryString(queryInfo.getQuery())
            .withCountQueryString(countQuery)
            .withNativeSql(false)
            .build();
    }

    public static <R> Builder<R> newBuilder(Class<R> responseType) {
        return new Builder<>(responseType);
    }

    @Override
    public Query<R> getQuery(final Session session, final Pageable pageable) {
        String sortedQuery = queryString;

        if (pageable.getSort().isSorted()) {
            final WMResultTransformer transformer = Transformers.aliasToMappedClass(responseType);
            if (nativeSql) {
                sortedQuery = QueryHelper.applySortingForNativeQuery(queryString, pageable.getSort(),
                    transformer, ((SessionFactoryImplementor) session.getSessionFactory()).getDialect());
            } else {
                sortedQuery = QueryHelper.applySortingForHqlQuery(queryString, pageable.getSort(), transformer);
            }
        }

        Query<R> hibernateQuery = createQuery(session, sortedQuery, responseType);

        if (!pageable.isUnpaged()) {
            hibernateQuery.setFirstResult((int) pageable.getOffset());
            hibernateQuery.setMaxResults(pageable.getPageSize());
        }

        return hibernateQuery;
    }

    @Override
    public Optional<Query<Number>> getCountQuery(final Session session) {
        if (StringUtils.isNotBlank(countQueryString)) {
            return Optional.of(createQuery(session, countQueryString, Number.class));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Query<R> getQuery(final Session session) {
        return createQuery(session, queryString, responseType);
    }

    @SuppressWarnings("unchecked")
    private <T> Query<T> createQuery(final Session session, String queryString, final Class<T> returnType) {
        final Query<T> hibernateQuery;
        if (nativeSql) {
            hibernateQuery = session.createNativeQuery(queryString);
            Transformers.aliasToMappedClassOptional(returnType).ifPresent(hibernateQuery::setResultTransformer);
        } else {
            hibernateQuery = session.createQuery(queryString);
            if (hibernateQuery.getReturnAliases() != null && hibernateQuery.getReturnAliases().length != 0) {
                Transformers.aliasToMappedClassOptional(returnType).ifPresent(hibernateQuery::setResultTransformer);
            }
        }

        return hibernateQuery;
    }

    public static final class Builder<R> {
        private String queryString;
        private String countQueryString;
        private boolean nativeSql;
        private Class<R> responseType;

        private Builder(Class<R> responseType) {
            this.responseType = responseType;
        }

        public Builder<R> withQueryString(final String val) {
            queryString = val;
            return this;
        }

        public Builder<R> withCountQueryString(final String val) {
            countQueryString = val;
            return this;
        }

        public Builder<R> withNativeSql(final boolean val) {
            nativeSql = val;
            return this;
        }

        public RuntimeQueryProvider<R> build() {
            return new RuntimeQueryProvider<>(this);
        }
    }
}
