package com.wavemaker.runtime.commons.variable;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class VariableTypeHelper {

    private static final Pattern variablePattern;
    private static final Map<String, VariableType> prefixVsType;

    static {
        final String typePrefixes = Arrays.stream(VariableType.values())
                .filter(VariableType::isVariable)
                .map(VariableType::name)
                .reduce((r, e) -> r + "|" + e)
                .get();
        variablePattern = Pattern.compile("(" + typePrefixes + ")__(.+)__.+");

        prefixVsType = Arrays.stream(VariableType.values())
                .filter(VariableType::isVariable)
                .collect(Collectors.toMap(VariableType::name, variableType -> variableType));
    }

    private static final int PREFIX_GROUP = 1;
    private static final int VARIABLE_NAME_GROUP = 2;

    public static Pair<VariableType, String> fromVariableName(String name) {
        VariableType type = VariableType.PROMPT;
        String variableName = name;

        final Matcher matcher = variablePattern.matcher(name);
        if (matcher.find()) {
            type = prefixVsType.get(matcher.group(PREFIX_GROUP));
            variableName = matcher.group(VARIABLE_NAME_GROUP);
        }

        return ImmutablePair.of(type, variableName);
    }

    public static String toVariableName(VariableType variableType, String variableName, String parameterName) {
        return variableType.name() + "__" + variableName + "__" + parameterName;
    }
}
