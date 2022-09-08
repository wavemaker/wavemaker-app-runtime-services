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
package com.wavemaker.runtime.data.export;

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeanUtils;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.data.util.JavaTypeUtils;

public class SimpleFieldValueProvider implements FieldValueProvider {

    private final String fieldName;
    private final Class<?> dataClass;

    public SimpleFieldValueProvider(String fieldName, Class<?> dataClass) {
        this.fieldName = fieldName;
        this.dataClass = dataClass;
    }

    @Override
    public Object getValue(Object object) {
        String[] nestedFields = fieldName.split("\\.");
        Object value = null;
        Object nestedRowData = object;
        Class<?> currentClass = this.dataClass;
        try {
            for (String name : nestedFields) {
                PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(currentClass, name);
                value = (nestedRowData == null) ? null : propertyDescriptor.getReadMethod().invoke(nestedRowData);
                if (value == null) {
                    break;
                }
                nestedRowData = value;
                Class<?> propertyType = propertyDescriptor.getPropertyType();
                if (!JavaTypeUtils.isKnownType(propertyType)) {
                    currentClass = propertyType;
                }
            }
            return value;
        } catch (Exception e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.invalid.field.name"), e);
        }
    }
}
