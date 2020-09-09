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
package com.wavemaker.runtime.data.export.nativesql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import com.wavemaker.runtime.data.export.QueryExtractor;
import com.wavemaker.runtime.data.transform.WMResultTransformer;

/**
 * @author <a href="mailto:anusha.dharmasagar@wavemaker.com">Anusha Dharmasagar</a>
 * @since 22/5/17
 */
public class NativeQueryExtractor implements QueryExtractor {

    private ResultSet resultSet;
    private WMResultTransformer resultTransformer;
    private int currentIndex;

    public NativeQueryExtractor(
            final ResultSet resultSet, final WMResultTransformer resultTransformer) {
        this.resultSet = resultSet;
        this.resultTransformer = resultTransformer;
    }

    @Override
    public boolean next() throws Exception {
        final boolean hasNext = resultSet.next();
        this.currentIndex++;
        return hasNext;
    }

    @Override
    public boolean isFirstRow() {
        //since isFirst() or getRow() methods in java.sql.ResultSet are not supported in few DBs.
        return currentIndex == 1;
    }

    @Override
    public Object getCurrentRow() throws Exception {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        Map<String, Object> columnDataMap = new HashMap<>();
        for (int colIndex = 1; colIndex <= metaData.getColumnCount(); colIndex++) {
            final String aliasName = metaData.getColumnLabel(colIndex);
            final Object value = resultSet.getObject(colIndex);
            columnDataMap.put(aliasName, value);
        }
        return resultTransformer.transformFromMap(columnDataMap);
    }
}