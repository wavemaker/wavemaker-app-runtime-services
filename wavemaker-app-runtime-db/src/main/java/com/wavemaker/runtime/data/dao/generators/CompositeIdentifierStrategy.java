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
package com.wavemaker.runtime.data.dao.generators;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 30/11/17
 */
public class CompositeIdentifierStrategy<Entity, Identifier> implements IdentifierStrategy<Entity, Identifier> {

    private List<PropertyDescriptor> idProperties;

    public CompositeIdentifierStrategy(Class<Identifier> idClass) {
        idProperties = Arrays.stream(idClass.getDeclaredFields())
            .map(field -> BeanUtils.getPropertyDescriptor(idClass, field.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> extract(final Identifier identifier) {
        Map<String, Object> valuesMap = new HashMap<>();

        idProperties.forEach(idProperty -> {
            try {
                valuesMap.put(idProperty.getName(), idProperty.getReadMethod().invoke(identifier));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.unable.to.get.identifier.property"), e, idProperty.getName());
            }
        });

        return valuesMap;
    }
}
