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
package com.wavemaker.runtime.data.converters;

import org.hibernate.HibernateException;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 10/2/17
 */
public class StringTypeConverter extends HibernateBackedJavaTypeConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringTypeConverter.class);

    public StringTypeConverter() {
        super(StringType.INSTANCE.getJavaTypeDescriptor());
    }

    @Override
    public Object fromDbValue(final Object value) {
        String convertedValue;
        try {
            convertedValue = (String) super.fromDbValue(value);
        } catch (HibernateException e) {
            if (byte[].class.equals(value.getClass())) {
                convertedValue = new String(((byte[]) value));
            } else {
                LOGGER.debug("Not a string instance type, using String.valueOf for conversion {}", e.getMessage());
                convertedValue = String.valueOf(value);
            }
        }
        return convertedValue;
    }
}
