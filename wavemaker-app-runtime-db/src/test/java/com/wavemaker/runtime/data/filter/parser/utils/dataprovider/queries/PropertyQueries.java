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

/**
 * @author Sujith Simon
 * Created on : 12/11/18
 */
public class PropertyQueries {
    private static final List<String> positiveQueries = new ArrayList<>();
    private static final List<String> negativeQueries = new ArrayList<>();

    static {
        positiveQueries.add("child is not null");
        positiveQueries.add("child is not null and child.grandChild is null");
        positiveQueries.add("child.grandChild is not null");
        positiveQueries.add("child is null or child.grandChild is null");

        negativeQueries.add("child > 0");
        negativeQueries.add("child != 123");
        negativeQueries.add("child.grandChild like '%string%'");
        negativeQueries.add("child.grandChild in (123,434,343)");
    }

    public static List<String> getPositiveQueries(Class<?> dataType) {
        return positiveQueries;

    }

    public static List<String> getNegativeQueries(Class<?> dataType) {
        return negativeQueries;
    }
}
