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
public class SqlInjectionQueries {

    private static final List<String> queires = new ArrayList<>();

    static {
        queires.add("${key}='' or true=true");
        queires.add("${key}='asdf' or 1=1");
        queires.add("${key}=105 OR 1=1");
        queires.add("${key}=123; delete TABLE Employee;");
        queires.add("${key}=''; delete TABLE Employee;");
        queires.add("${key}=2 or 1 is not null");
    }

    public static List<String> getQueries(Class<?> dataType) {
        return HqlParserTestUtils.getNormalizedHql(dataType, queires, false);
    }
}
