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

package com.wavemaker.runtime.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;

public class WMObjectReader extends ObjectReader {

    private final ObjectMapper wmObjectMapper = WMObjectMapper.getInstance().getReaderObjectMapper();

    protected WMObjectReader(ObjectMapper mapper, DeserializationConfig config) {
        super(mapper, config);
    }

    private WMObjectReader(ObjectReader objectReader, DeserializationConfig config, JavaType valueType,
                           JsonDeserializer<Object> rootDeser, Object valueToUpdate, FormatSchema schema,
                           InjectableValues injectableValues, DataFormatReaders det) {
        super(objectReader, config, valueType, rootDeser, valueToUpdate, schema, injectableValues, det);
    }

    @Override
    public ObjectReader forType(JavaType valueType) {
        if (valueType != null && valueType.equals(getValueType())) {
            return this;
        }
        JsonDeserializer<Object> rootDeser = _prefetchRootDeserializer(valueType);
        // type is stored here, no need to make a copy of config
        DataFormatReaders det = _dataFormatReaders;
        if (det != null) {
            det = det.withType(valueType);
        }
        return new WMObjectReader(this, getConfig(), valueType, rootDeser,
            _valueToUpdate, _schema, _injectableValues, det);
    }

    @Override
    public <T> T readValue(InputStream src) throws IOException {
        if (String.class.equals(this.getValueType().getRawClass())) {
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(src, stringWriter, StandardCharsets.UTF_8);
            return (T) stringWriter.toString();
        }
        return wmObjectMapper.readValue(src, this.getValueType());
    }
}
