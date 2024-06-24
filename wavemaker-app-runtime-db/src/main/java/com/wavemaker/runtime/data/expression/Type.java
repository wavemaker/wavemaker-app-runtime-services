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
package com.wavemaker.runtime.data.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.data.util.QueryParserConstants;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 17/2/16
 */
public enum Type implements Criteria {

    STARTING_WITH("startswith") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.select(from).where(builder.like(from.get(name), value + "%"));
        }
    }, ENDING_WITH("endswith") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.select(from).where(builder.like(from.get(name), "%" + value));
        }
    }, CONTAINING("containing") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.select(from).where(builder.like(from.get(name), "%" + value + "%"));
        }
    }, EQUALS("=") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            if (value == null) {
                throw new IllegalArgumentException("Equals expression should not have null value, either collection or array or primitive values supported.");
            }

            if (value instanceof Collection) {
                final Collection<Integer> values = (Collection<Integer>) value;
                if (values.isEmpty()) {
                    throw new IllegalArgumentException("Equals expression should have a collection/array of values with at-least one entry.");
                }
                criteria.select(from).where(from.get(name).in(value));
            } else if (value.getClass().isArray()) {
                final Object[] values = (Object[]) value;
                if (values.length == 0) {
                    throw new IllegalArgumentException("Equals expression should have a collection/array of values with at-least one entry.");
                }
                criteria.select(from).where(from.get(name).in(value));
            } else {
                criteria.select(from).where(builder.equal(from.get(name), value));
            }
            return criteria;
        }
    }, NOT_EQUALS("!=") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.select(from).where(builder.notEqual(from.get(name), value));
        }
    }, BETWEEN("between") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            if (value instanceof Collection) {
                Collection collection = (Collection) value;
                if (collection.size() != 2) {
                    throw new IllegalArgumentException("Between expression should have a collection/array of values with just two entries.");
                }

                Iterator iterator = collection.iterator();
                criteria.where(builder.and(builder.ge((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) iterator.next())),
                    builder.le((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) iterator.next()));
            } else if (value.getClass().isArray()) {
                Object[] array = (Object[]) value;
                if (array.length != 2) {
                    throw new IllegalArgumentException("Between expression should have a array/array of values with just two entries.");
                }

                criteria.where(builder.and(builder.ge((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) array[0])),
                    builder.le((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) array[1]));
            } else {
                throw new IllegalArgumentException("Between expression should have a collection/array of values with just two entries.");
            }
            return criteria;
        }
    }, LESS_THAN("<") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.where(builder.lt((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) value));
        }
    }, LESS_THAN_OR_EQUALS("<=") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.where(builder.le((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) value));
        }
    }, GREATER_THAN(">") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.where(builder.gt((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) value));
        }
    }, GREATER_THAN_OR_EQUALS(">=") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.where(builder.ge((Expression<? extends Number>) from.get(name), (Expression<? extends Number>) value));
        }
    }, NULL("null") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.where(builder.isNull(from.get(name)));
        }
    }, EMPTY("empty") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.where(builder.equal(from.get(name), ""));
        }
    }, LIKE("like") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, String name, Object value) {
            return criteria.where(builder.like(from.get(name), String.valueOf(value)));
        }
    }, IN("in") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, String name, Object value) {
            if (value instanceof Collection) {
                return criteria.select(from).where(from.get(name).in((Collection) value));
            }
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.unexpected.value.type"), value.getClass());
        }
    }, NULL_OR_EMPTY("nullorempty") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, final String name, final Object value) {
            return criteria.where(builder.or(builder.equal(from.get(name), ""), builder.isNull(from.get(name))));
        }
    }, IS("is") {
        @Override
        public CriteriaQuery criterion(CriteriaBuilder builder, CriteriaQuery criteria, Root from, String name, Object value) {
            String castedValue = (String) value;
            if (QueryParserConstants.NULL.equalsIgnoreCase(castedValue)) {
                return NULL.criterion(builder, criteria, from, name, value);
            } else if (QueryParserConstants.NOTNULL.equalsIgnoreCase(castedValue)) {
                return criteria.where(builder.not((Expression<Boolean>) NULL.criterion(builder, criteria, from, name, value)));
            } else if (QueryParserConstants.NULL_OR_EMPTY.equalsIgnoreCase(castedValue)) {
                return NULL_OR_EMPTY.criterion(builder, criteria, from, name, value);
            } else if (QueryParserConstants.EMPTY.equalsIgnoreCase(castedValue)) {
                return EMPTY.criterion(builder, criteria, from, name, value);
            }
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.invalid.IS.operator.value"), value.getClass());
        }
    };

    static Map<String, Type> nameVsType = new HashMap<>();

    static {
        for (Type type : Type.values()) {
            nameVsType.put(type.getName(), type);
        }
    }

    private String name;

    Type(String name) {
        this.name = name;
    }

    public static Type valueFor(String typeName) {
        return nameVsType.get(typeName.toLowerCase());

    }

    public String getName() {
        return name;
    }
}
