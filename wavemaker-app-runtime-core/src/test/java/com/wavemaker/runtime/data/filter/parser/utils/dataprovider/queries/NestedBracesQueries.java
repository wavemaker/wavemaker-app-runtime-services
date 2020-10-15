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
package com.wavemaker.runtime.data.filter.parser.utils.dataprovider.queries;

import java.util.ArrayList;
import java.util.List;

import com.wavemaker.runtime.data.filter.parser.utils.HqlParserTestUtils;

/**
 * @author Sujith Simon
 * Created on : 12/11/18
 */
public class NestedBracesQueries {
    private static final List<String> positiveQueries = new ArrayList<>();
    private static final List<String> negativeQueries = new ArrayList<>();

    static {
        positiveQueries.add("(${key}=${value})");
        positiveQueries.add("((child.${key} = ${value}) or ${key} != ${value})");
        positiveQueries.add("(${key}=${value}) or (child.grandChild.${key}=${value} or (child.${key}=${value} and (${key}=${value})))");
        positiveQueries.add("(${key} >= ${value} or child.${key} is not null)");

        negativeQueries.add("(${key} != ${value} ${key} is not null");
        negativeQueries.add("(${key} in (${value}, ${value})");
        negativeQueries.add("(${key} = ( ${value} ))");
        negativeQueries.add("(${key} = ${value} or ${key} = ) ${value}");
    }

    public static List<String> getPositiveQueries(Class<?> dataType) {
        return HqlParserTestUtils.getNormalizedHql(dataType, positiveQueries);

    }

    public static List<String> getNegativeQueries(Class<?> dataType) {
        return HqlParserTestUtils.getNormalizedHql(dataType, negativeQueries);
    }
}
