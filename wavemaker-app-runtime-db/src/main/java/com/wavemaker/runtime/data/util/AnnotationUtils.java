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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 30/11/17
 */
public abstract class AnnotationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationUtils.class);

    public static List<PropertyDescriptor> findProperties(Class<?> type, Class<? extends Annotation> annotationType) {
        return Arrays.stream(type.getDeclaredFields())
                .map(field -> ImmutablePair.of(field, BeanUtils.getPropertyDescriptor(type, field.getName())))
                .filter(pair -> {
                    Field field = pair.getLeft();
                    PropertyDescriptor propertyDescriptor = pair.getRight();
                    boolean found = field.isAnnotationPresent(annotationType);
                    if (propertyDescriptor != null) {
                        if (propertyDescriptor.getReadMethod() != null) {
                            found = found || propertyDescriptor.getReadMethod().isAnnotationPresent(annotationType);
                        } else {
                            LOGGER.warn("Read method not found for field: {} in class: {}", field.getName(),
                                    type.getName());
                        }

                        if (propertyDescriptor.getWriteMethod() != null) {
                            found = found || propertyDescriptor.getWriteMethod().isAnnotationPresent(annotationType);
                        } else {
                            LOGGER.warn("Write method not found for field: {} in class: {}", field.getName(),
                                    type.getName());
                        }
                    } else {
                        LOGGER.warn("Property Descriptor not found for field: {} in class: {}", field.getName(),
                                type.getName());
                    }

                    return found;
                }).map(pair -> pair.getRight())
                .collect(Collectors.toList());
    }

    public static List<PropertyDescription> findProperties(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(field -> new PropertyDescription(field, BeanUtils.getPropertyDescriptor(type, field.getName())))
                .collect(Collectors.toList());
    }
}
