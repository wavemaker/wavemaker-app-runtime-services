package com.wavemaker.runtime.data.filter.parser;

import java.lang.reflect.Field;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.wavemaker.runtime.data.annotations.ColumnAlias;
import com.wavemaker.runtime.data.filter.parser.antlr4.HqlFilterParser;

public class HqlToNativeSqlConverter {

    public String convertHqlToNative(String hqlQuery, Class<?> entity) {
        ParseTree rootNode = HqlParser.getInstance().getRootNode(hqlQuery);
        HqlFilterPropertyResolver resolver = new HqlFilterPropertyResolverImpl(entity);
        HqlParserContext hqlParserContext = new HqlParserContext(resolver);
        parse(rootNode, hqlParserContext);
        return hqlParserContext.getQueryBuilder().toString();
    }

    private void parse(ParseTree rule, HqlParserContext hqlParserContext) {
        for (int index = 0; index < rule.getChildCount(); index++) {
            ParseTree child = rule.getChild(index);
            if (child instanceof HqlFilterParser.ExpressionContext) {
                HqlFilterParser.ExpressionContext expression = (HqlFilterParser.ExpressionContext) child;
                HqlFilterPropertyResolver propertyResolver = hqlParserContext.getHqlFilterPropertyResolver();
                ParseTree key = expression.key();
                resolveKey(key, hqlParserContext, propertyResolver);
                ParseTree condition = expression.condition();
                resolveCondition(condition, hqlParserContext);
            } else if (child instanceof TerminalNodeImpl) {
                hqlParserContext.appendQuery(child.getText());
            } else {
                parse(child, hqlParserContext);
            }
        }
    }

    private void resolveKey(ParseTree node, HqlParserContext hqlParserContext, HqlFilterPropertyResolver propertyResolver) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof TerminalNodeImpl) {
                TerminalNodeImpl terminalNode = ((TerminalNodeImpl) child);
                if (terminalNode.getSymbol().getType() == HqlFilterParser.KEY) {
                    String fieldName = terminalNode.getText();
                    Field keyField = propertyResolver.findField(fieldName);
                    String columnName = keyField.getAnnotation(ColumnAlias.class).value();
                    hqlParserContext.appendQuery("tempTable." + columnName);
                } else {
                    hqlParserContext.appendQuery(child.getText());
                }
            } else {
                resolveCondition(child, hqlParserContext);
            }
        }
    }

    private void resolveCondition(ParseTree node, HqlParserContext hqlParserContext) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof TerminalNodeImpl) {
                hqlParserContext.appendQuery(child.getText());
            } else {
                resolveCondition(child, hqlParserContext);
            }
        }
    }
}
