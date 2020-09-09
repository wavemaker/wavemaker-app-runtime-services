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
package com.wavemaker.runtime.data.filter.wmfunctions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wavemaker.runtime.data.filter.QueryInterceptor;
import com.wavemaker.runtime.data.filter.WMQueryInfo;

/**
 * @author Sujith Simon
 * Created on : 21/1/19
 */

/**
 * @deprecated - WMHQL functions are not longer supported. This class will be removed
 * once the WMHQL functions are not included in the HTTP requests containing queries.
 */
public class WMQueryFunctionsRemoverInterceptor implements QueryInterceptor {

    private static final String functionsPattern = "wm_.*?\\((.*?)\\)";
    private static final Pattern pattern = Pattern.compile(functionsPattern, Pattern.CASE_INSENSITIVE);

    @Override
    public void intercept(final WMQueryInfo queryInfo, Class<?> entity) {

        final Matcher matcher = pattern.matcher(queryInfo.getQuery());
        StringBuffer newQuerySB = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(newQuerySB, matcher.group(1));

        }
        matcher.appendTail(newQuerySB);
        queryInfo.setQuery(newQuerySB.toString());
    }

}
