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
package com.wavemaker.runtime.data.converters;

import java.sql.Blob;

import org.hibernate.type.BlobType;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 23/2/17
 */
public class BlobTypeConverter extends HibernateBackedJavaTypeConverter {
    public BlobTypeConverter() {
        super(BlobType.INSTANCE.getJavaTypeDescriptor());
    }

    @Override
    public Object fromDbValue(final Object value) {
        if (value instanceof Blob) {
            return toDbValue(value, byte[].class);
        } else {
            return value;
        }
    }

    @Override
    public Object toDbValue(final Object value, final Class<?> toType) {
        return value instanceof Blob ? super.toDbValue(value, toType) : value;
    }
}
