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
package com.wavemaker.runtime.data.filter.parser.utils.dataprovider.queries;

import java.util.ArrayList;
import java.util.List;

import com.wavemaker.runtime.data.filter.parser.utils.HqlParserTestUtils;

/**
 * @author Sujith Simon
 * Created on : 9/11/18
 */
public class LikeQueries {

    private static final List<String> positiveQueries = new ArrayList<>();
    private static final List<String> negativeQueries = new ArrayList<>();

    static {
        positiveQueries.add("${key} like '%string%'");
        positiveQueries.add("child.${key} like '%string'");
        positiveQueries.add("child.grandChild.${key} like 'string%string'");
        positiveQueries.add("${key} like '%string' or child.${key} like '%string'");

        negativeQueries.add("${key} like like '%string%'");
        negativeQueries.add("child.${key} like 123 ");
        negativeQueries.add("child.grandChild.${key} like 'string% ");
        negativeQueries.add("${key} like '%string%' or '%string%' ");
    }

    public static List<String> getPositiveQueries(Class<?> dataType) {
        return HqlParserTestUtils.getNormalizedHql(dataType, positiveQueries, false);
    }

    public static List<String> getNegativeQueries(Class<?> dataType) {
        return HqlParserTestUtils.getNormalizedHql(dataType, negativeQueries, false);
    }

}
