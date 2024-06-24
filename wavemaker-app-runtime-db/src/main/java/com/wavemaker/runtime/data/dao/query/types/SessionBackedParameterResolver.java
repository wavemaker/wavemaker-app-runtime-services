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
package com.wavemaker.runtime.data.dao.query.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.named.NamedObjectRepository;
import org.hibernate.query.named.NamedQueryMemento;
import org.hibernate.type.spi.TypeConfiguration;

import com.wavemaker.runtime.data.util.HibernateUtils;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 21/7/17
 */
public class SessionBackedParameterResolver {

    private final SessionFactoryImplementor factory;

    private final Map<String, ParameterTypeResolver> resolversCache;

    public SessionBackedParameterResolver(final SessionFactoryImplementor factory) {
        this.factory = factory;

        resolversCache = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public ParameterTypeResolver getResolver(String queryName) {
        return resolversCache.computeIfAbsent(queryName, name -> {
            Map<String, Type> typesMap = new HashMap<>();

            final NamedObjectRepository repository = factory.getQueryEngine().getNamedObjectRepository();

            NamedQueryMemento definition = repository.getSqmQueryMemento(name);

            if (definition == null) {
                definition = repository.getNativeQueryMemento(name);
            }

            //need to get parameters
            final Map<String, String> parameterTypes = null;

            if (parameterTypes != null) {
                TypeConfiguration typeConfiguration = factory.getTypeConfiguration();
                parameterTypes.forEach((paramName, paramType) -> {
                    final Optional<Type> typeOptional = HibernateUtils.findType(typeConfiguration, paramType);
                    typeOptional.ifPresent(type -> typesMap.put(paramName, type));
                });
            }

            return parameterName -> Optional.ofNullable(typesMap.get(parameterName));
        });
    }

}
