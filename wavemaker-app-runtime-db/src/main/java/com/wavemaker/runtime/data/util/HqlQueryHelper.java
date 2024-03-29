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
package com.wavemaker.runtime.data.util;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.hibernate5.HibernateTemplate;

import com.wavemaker.runtime.data.dao.callbacks.PaginatedQueryCallback;
import com.wavemaker.runtime.data.dao.callbacks.QueryCallback;
import com.wavemaker.runtime.data.dao.query.providers.AppRuntimeParameterProvider;
import com.wavemaker.runtime.data.dao.query.providers.ParametersProvider;
import com.wavemaker.runtime.data.dao.query.providers.RuntimeQueryProvider;
import com.wavemaker.runtime.data.dao.query.types.wmql.WMQLTypeHelper;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.hql.SelectQueryBuilder;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 30/11/17
 */
public class HqlQueryHelper {

    public static <R> Page<R> execute(
        HibernateTemplate template, Class<R> returnType, SelectQueryBuilder builder,
        Pageable pageable, WMQLTypeHelper wmqlTypeHelper) {

        final WMQueryInfo queryInfo = builder.build();

        final RuntimeQueryProvider<R> queryProvider = RuntimeQueryProvider.from(queryInfo, returnType);
        ParametersProvider parametersProvider = new AppRuntimeParameterProvider(queryInfo, template.getSessionFactory().getTypeHelper(), wmqlTypeHelper);

        return template
            .execute(new PaginatedQueryCallback<>(queryProvider, parametersProvider, pageable));
    }

    public static <R> Optional<R> execute(
        HibernateTemplate template, Class<R> returnType, SelectQueryBuilder builder, WMQLTypeHelper wmqlTypeHelper) {

        final WMQueryInfo queryInfo = builder.build();

        final RuntimeQueryProvider<R> queryProvider = RuntimeQueryProvider.from(queryInfo, returnType);

        ParametersProvider parametersProvider = new AppRuntimeParameterProvider(queryInfo, template.getSessionFactory().getTypeHelper(), wmqlTypeHelper);

        return template.execute(new QueryCallback<>(queryProvider, parametersProvider));
    }
}
