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
package com.wavemaker.runtime.data.filter.parser.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;
import com.wavemaker.runtime.data.filter.parser.utils.dataprovider.FieldsMetadata;

/**
 * @author Sujith Simon
 * Created on : 9/11/18
 */
public interface HqlParserTestUtils {

    static List<String> getNormalizedHql(Class<?> dataType, List<String> queryList) {
        return getNormalizedHql(dataType, queryList, true);
    }

    static List<String> getNormalizedHql(Class<?> dataType, List<String> queryList, boolean includesValues) {
        List<String> queries = new ArrayList<>();
        String fieldName = getFieldName(dataType);
        Object[] values = FieldsMetadata.getSampleValues(dataType);
        for (String query : queryList) {
            if (includesValues) {
                for (Object value : values) {
                    queries.add(StringSubstitutor.replace(query, ImmutableMap.of("key", fieldName,
                        "value", getObjectValue(value))));
                }
            } else {
                queries.add(StringSubstitutor.replace(query, Collections.singletonMap("key", fieldName)));
            }

        }
        return queries;
    }

    static String getFieldName(Class<?> dataType) {
        return "wm" + dataType.getSimpleName();
    }

    static String getObjectValue(Object value) {
        return value instanceof String ? "'" + value + "'" : String.valueOf(value);
    }

}
