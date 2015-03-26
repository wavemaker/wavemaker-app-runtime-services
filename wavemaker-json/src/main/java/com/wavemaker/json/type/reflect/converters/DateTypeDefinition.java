/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.json.type.reflect.converters;

import java.io.IOException;
import java.io.Writer;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import com.wavemaker.common.MessageResource;
import com.wavemaker.common.WMRuntimeException;
import com.wavemaker.json.JSONMarshaller;
import com.wavemaker.json.type.converters.ReadObjectConverter;
import com.wavemaker.json.type.converters.WriteObjectConverter;
import com.wavemaker.json.type.reflect.PrimitiveReflectTypeDefinition;
import com.wavemaker.json.type.reflect.ReflectTypeUtils;

/**
 * Date primitive type. This includes all known subclasses of {@link Date}, including {@link java.sql.Date},
 * {@link Time}, and {@link Timestamp}. These will serialize to and from Number objects sent from the client, assuming
 * that the numbers represent milliseconds since the epoch (standard Java time, as well).
 * 
 * @author Matt Small
 */
public class DateTypeDefinition extends PrimitiveReflectTypeDefinition implements ReadObjectConverter, WriteObjectConverter {

    public DateTypeDefinition(Class<? extends Date> klass) {

        super();
        this.setKlass(klass);
        this.setTypeName(ReflectTypeUtils.getTypeName(this.getKlass()));
    }

    @Override
    public Object readObject(Object input, Object root, String path) {

        if (input == null) {
            return null;
        } else if (Number.class.isAssignableFrom(input.getClass())) {

            Number num = (Number) input;

            if (java.util.Date.class.equals(this.getKlass())) {
                return new java.util.Date(num.longValue());
            } else if (java.sql.Date.class.equals(this.getKlass())) {
                return new java.sql.Date(num.longValue());
            } else if (java.sql.Timestamp.class.equals(this.getKlass())) {
                return new java.sql.Timestamp(num.longValue());
            } else if (java.sql.Time.class.equals(this.getKlass())) {
                return new java.sql.Time(num.longValue());
            } else {
                throw new WMRuntimeException(MessageResource.JSON_UNHANDLED_TYPE, input, input.getClass());
            }
        } else {
            return input;
        }
    }

    @Override
    public void writeObject(Object input, Object root, String path, Writer writer) throws IOException {

        if (input == null) {
            JSONMarshaller.marshal(writer, input);
        } else if (java.util.Date.class.isAssignableFrom(input.getClass())) {
            writer.write("" + ((java.util.Date) input).getTime());
        } else {
            throw new WMRuntimeException(MessageResource.JSON_UNHANDLED_TYPE, input, input.getClass());
        }
    }
}