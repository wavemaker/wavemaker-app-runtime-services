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
package com.wavemaker.runtime.data.filter.parser;

import java.lang.reflect.Field;
import java.util.Optional;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.runtime.data.dao.validators.HqlPropertyResolver;
import com.wavemaker.runtime.data.exception.HqlGrammarException;
import com.wavemaker.runtime.data.model.JavaType;
import com.wavemaker.runtime.data.util.JavaTypeUtils;

/**
 * @author Sujith Simon
 * Created on : 1/11/18
 */
public class HqlFilterPropertyResolverImpl implements HqlFilterPropertyResolver {

    private Class<?> entity;

    public HqlFilterPropertyResolverImpl(Class<?> entity) {
        this.entity = entity;
    }

    @Override
    public Field findField(String propertyKey) {
        Optional<Field> optionalField = HqlPropertyResolver.findField(propertyKey, entity);
        if (!optionalField.isPresent()) {
            throw new HqlGrammarException(MessageResource.create("Property {0} in the class {1} is not valid."), propertyKey, entity.getName());
        }
        return optionalField.get();
    }

    @Override
    public JavaType findJavaType(Field field) {
        JavaType javaType = JavaTypeUtils.fromClassName(field.getType().getName()).orElse(null);
        if (javaType == null) {
            throw new HqlGrammarException(MessageResource.create("The property {0} in the entity {1} is not a comparable data type."), field, entity.getName());
        }
        return javaType;
    }

}
