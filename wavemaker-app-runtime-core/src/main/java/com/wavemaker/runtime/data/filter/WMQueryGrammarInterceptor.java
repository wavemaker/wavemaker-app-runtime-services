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
