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
package com.wavemaker.runtime.data.transform;

import java.util.Map;

import org.hibernate.transform.ResultTransformer;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 17/11/16
 */
public interface WMResultTransformer extends ResultTransformer {

    Object transformFromMap(Map<String, Object> resultMap);

    String aliasToFieldName(String columnName);

    String aliasFromFieldName(String fieldName);

    boolean containsField(String fieldName);

    static String getAlias(String[] aliases, int index) {
        String alias = Integer.toString(index);
        // fix for hibernate 5, aliases returning null for HQL if no alias specified in query.
        if (aliases != null && aliases[index] != null) {
            alias = aliases[index];
        }
        return alias;
    }
}
