package com.wavemaker.runtime.data.dao.query.providers;

import java.util.Optional;

import javax.persistence.Entity;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.query.Query;
import org.hibernate.query.spi.NamedQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.wavemaker.runtime.data.dao.util.QueryHelper;
import com.wavemaker.runtime.data.filter.parser.HqlToNativeSqlConverter;
import com.wavemaker.runtime.data.transform.Transformers;
import com.wavemaker.runtime.data.transform.WMResultTransformer;

public class SessionBackedRuntimeQueryProvider<R> implements QueryProvider<R>, PaginatedQueryProvider<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionBackedRuntimeQueryProvider.class);

    private String name;
    private Class<R> responseType;
    private String filterQuery;
    private String queryString;
    private String countQueryString;

    public SessionBackedRuntimeQueryProvider(final String name, final String filterQuery, final Class<R> responseType) {
        this.name = name;
        this.filterQuery = filterQuery;
        this.responseType = responseType;
    }

    @Override
    public Query<R> getQuery(final Session session) {
        return getAndConfigureQuery(session, name, responseType);
    }

    @Override
    public Query<R> getQuery(final Session session, final Pageable pageable) {
        Query<R> query;
        final Sort sort = pageable.getSort();
        final WMResultTransformer transformer = Transformers.aliasToMappedClass(responseType);

        if (sort != null) {
            query = createSortedQuery(session, sort, transformer);
        } else {
            query = getAndConfigureQuery(session, name, responseType);
        }

        query.setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        return query;
    }

    @Override
    public Optional<Query<Number>> getCountQuery(final Session session) {
        Query<Number> countQuery = null;

        final String countQueryName = this.name + "__count";
        final NamedQueryRepository repository = ((SessionFactoryImplementor) session.getSessionFactory())
                .getNamedQueryRepository();
        if (queryExists(repository, countQueryName)) {
            boolean isNativeSql = isNativeSql(session, name);
            queryString = isNativeSql ? getNativeQueryWithFilter(session, name) : getHqlQuery(session, name);
            countQueryString = QueryHelper.getCountQuery(queryString, isNativeSql(session, name));
            if (isNativeSql) {
                countQuery = session.createNativeQuery(countQueryString);
                Transformers.aliasToMappedClassOptional(Number.class).ifPresent(countQuery::setResultTransformer);
            } else {
                countQuery = session.createQuery(countQueryString, Number.class);
            }
        } else {
            LOGGER.debug("Count query not found for query:{}", name);
        }

        return Optional.ofNullable(countQuery);
    }

    @SuppressWarnings("unchecked")
    private <T> Query<T> getAndConfigureQuery(final Session session, String name, Class<T> type) {
        final Query<T> query;
        if (isNativeSql(session, name)) {
            if (queryString == null) {
                queryString = getNativeQueryWithFilter(session, name);
            }
            query = session.createNativeQuery(queryString);
            Transformers.aliasToMappedClassOptional(type).ifPresent(query::setResultTransformer);
        } else {
            if (queryString == null) {
                queryString = getHqlQuery(session, name);
            }
            query = session.createQuery(queryString, type);
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    private Query<R> createSortedQuery(final Session session, final Sort sort, final WMResultTransformer transformer) {
        final Query<R> query;
        boolean isNativeSql = isNativeSql(session, name);

        if (!isNativeSql) {
            queryString = getHqlQuery(session, name);
            String sortedQuery = QueryHelper
                    .applySortingForHqlQuery(queryString, sort,
                            transformer);
            if (isMappedType(responseType)) {
                query = session.createQuery(sortedQuery, responseType);
            } else {
                query = session.createQuery(sortedQuery).setResultTransformer(transformer);
            }
        } else {
            queryString = getNativeQuery(session, name);
            String sortedQuery = QueryHelper
                    .applySortingForNativeQuery(queryString,
                            sort, transformer,
                            ((SessionFactoryImplementor) session.getSessionFactory()).getDialect());
            sortedQuery = addFilterCondition(sortedQuery, true);
            query = session.createNativeQuery(sortedQuery).setResultTransformer(transformer);
        }
        return query;
    }

    private boolean queryExists(final NamedQueryRepository repository, final String countQueryName) {
        return repository.getNamedQueryDefinition(countQueryName) != null || repository.getNamedSQLQueryDefinition(countQueryName) != null;
    }

    private boolean isMappedType(Class<?> type) {
        return type.isAnnotationPresent(Entity.class);
    }

    private boolean isNativeSql(final Session session, String name) {
        final NamedQueryRepository repository = ((SessionFactoryImplementor) session.getSessionFactory())
                .getNamedQueryRepository();
        if (repository.getNamedQueryDefinition(name) != null) {
            return false;
        } else if (repository.getNamedSQLQueryDefinition(name) != null) {
            return true;
        } else {
            throw ((SessionImplementor) session).getExceptionConverter()
                    .convert(new IllegalArgumentException("No query defined for that name [" + name + "]"));
        }
    }

    private String getNativeQuery(final Session session, String name) {
        final NamedQueryRepository repository = ((SessionFactoryImplementor) session.getSessionFactory())
                .getNamedQueryRepository();
        return repository.getNamedSQLQueryDefinition(name).getQueryString();
    }

    private String getNativeQueryWithFilter(final Session session, String name) {
        final NamedQueryRepository repository = ((SessionFactoryImplementor) session.getSessionFactory())
                .getNamedQueryRepository();
        String nativeQuery = repository.getNamedSQLQueryDefinition(name).getQueryString();
        return addFilterCondition(nativeQuery, true);
    }

    private String getHqlQuery(final Session session, String name) {
        final NamedQueryRepository repository = ((SessionFactoryImplementor) session.getSessionFactory())
                .getNamedQueryRepository();
        return repository.getNamedQueryDefinition(name).getQueryString();
    }

    private String addFilterCondition(String mainQuery, boolean isNativeSql) {
        if (filterQuery != null) {
            final StringBuilder builder = new StringBuilder();
            if (isNativeSql) {
                builder.append("select *from ( ").append(mainQuery).append(" ) as tempTable where ");
                HqlToNativeSqlConverter hqlToNativeSqlConverter = new HqlToNativeSqlConverter();
                String nativeSqlQuery = hqlToNativeSqlConverter.convertHqlToNative(filterQuery, responseType);
                builder.append(nativeSqlQuery);
                return builder.toString();
            }
        }
        return mainQuery;
    }

}
