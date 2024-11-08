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
package com.wavemaker.runtime.data.filter.parser;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.wavemaker.runtime.data.filter.parser.antlr4.HqlFilterParser;
import com.wavemaker.runtime.data.model.JavaType;
import com.wavemaker.runtime.data.util.QueryParserConstants;

/**
 * @author Sujith Simon
 * Created on : 6/11/18
 */
public class HqlExpressionParser {

    private static final List<Integer> valueTerminals = Arrays.asList(
        HqlFilterParser.NUMBER_VALUE,
        HqlFilterParser.STRING_VALUE,
        HqlFilterParser.NULL,
        HqlFilterParser.BOOLEAN_VALUE
    );
    private static final List<Class<?>> nullContexts = Arrays.asList(
        HqlFilterParser.IsNullContext.class,
        HqlFilterParser.IsNotNullContext.class
    );
    private static final String PARAMETER_PREFIX = "wm_parsed_param";
    private ParseTree condition;
    private HqlFilterParser.KeyContext keyContext;

    public HqlExpressionParser(HqlFilterParser.ExpressionContext expression) {
        keyContext = expression.key();
        condition = expression.condition();
    }

    public void parse(HqlParserContext hqlParserContext) {
        String key = keyContext.getToken(HqlFilterParser.KEY, 0).getText();
        HqlFilterPropertyResolver propertyResolver = hqlParserContext.getHqlFilterPropertyResolver();

        Field keyField = propertyResolver.findField(key);

        JavaType keyJavaType = null;

        if (!nullContexts.contains(condition.getChild(0).getClass())) {
            keyJavaType = propertyResolver.findJavaType(keyField);
        }

        hqlParserContext.appendQuery(keyContext.getText());
        resolveCondition(condition, hqlParserContext, keyJavaType);
    }

    private void resolveCondition(ParseTree node, HqlParserContext hqlParserContext, JavaType keyJavaType) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof TerminalNodeImpl terminalNode) {
                if (valueTerminals.contains(terminalNode.getSymbol().getType())) {
                    String value = terminalNode.getText();

                    if (!QueryParserConstants.NULL.equalsIgnoreCase(value)) {
                        value = value.replaceAll("^'|'$", ""); // Remove wrapping single quotes.
                        String placeHolderKey = getNextPlaceHolderKey(hqlParserContext);

                        hqlParserContext.addParameter(placeHolderKey, value, keyJavaType);
                        hqlParserContext.appendQuery(':' + placeHolderKey);
                    } else {
                        hqlParserContext.appendQuery(value);
                    }
                } else {
                    hqlParserContext.appendQuery(child.getText());
                }
            } else {
                resolveCondition(child, hqlParserContext, keyJavaType);
            }
        }
    }

    private String getNextPlaceHolderKey(HqlParserContext hqlParserContext) {
        return PARAMETER_PREFIX + hqlParserContext.getParameters().size();
    }

}
