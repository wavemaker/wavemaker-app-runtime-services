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
package com.wavemaker.runtime.data.dao.generators;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import com.wavemaker.runtime.data.util.AnnotationUtils;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 30/11/17
 */
public class SingleIdentifierStrategy<Entity, Identifier> implements IdentifierStrategy<Entity, Identifier> {

    private final String idFieldName;

    public SingleIdentifierStrategy(Class<Entity> entityClass) {
        final List<PropertyDescriptor> idProperties = AnnotationUtils.findProperties(entityClass, Id.class);
        idFieldName = idProperties.get(0).getName();
    }

    @Override
    public Map<String, Object> extract(final Identifier identifier) {
        return Collections.singletonMap(idFieldName, identifier);
    }
}
