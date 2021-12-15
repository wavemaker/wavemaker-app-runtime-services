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
package com.wavemaker.runtime.data.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.wavemaker.runtime.data.filter.parser.HqlFilterPropertyResolver;
import com.wavemaker.runtime.data.filter.parser.HqlFilterPropertyResolverImpl;
import com.wavemaker.runtime.data.filter.parser.HqlParser;

/**
 * @author Sujith Simon
 * Created on : 30/10/18
 */
public class WMQueryGrammarInterceptor implements QueryInterceptor {

    private static final Map<String, String> paramValueReplacements = new HashMap<>();

    static {
        paramValueReplacements.put("''", "'");
    }

    @Override
    public void intercept(WMQueryInfo queryInfo, Class<?> entity) {

        HqlFilterPropertyResolver resolver = new HqlFilterPropertyResolverImpl(entity);
        WMQueryInfo parsedQuery = HqlParser.getInstance().parse(queryInfo.getQuery(), resolver);

        removeEscapeCharactersFromParameterValues(parsedQuery);

        queryInfo.setQuery(parsedQuery.getQuery());
        queryInfo.getParameters().putAll(parsedQuery.getParameters());
    }

    private void removeEscapeCharactersFromParameterValues(WMQueryInfo wmQueryInfo) {
        for (WMQueryParamInfo param : wmQueryInfo.getParameters().values()) {
            for (Map.Entry<String, String> paramValueReplacement : paramValueReplacements.entrySet()) {
                String paramValue = Objects.toString(param.getValue(), "");
                if (paramValue.contains(paramValueReplacement.getKey())) {
                    param.setValue(paramValue.replaceAll(paramValueReplacement.getKey(), paramValueReplacement.getValue()));
                }
            }
        }
    }
}
