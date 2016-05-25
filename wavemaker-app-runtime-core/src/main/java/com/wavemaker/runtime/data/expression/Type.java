/**
 * Copyright © 2013 - 2016 WaveMaker, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 17/2/16
 */
public enum Type implements Criteria {

    STARTING_WITH("startswith") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.ilike(name, String.valueOf(value), MatchMode.START);
        }
    }, ENDING_WITH("endswith") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.ilike(name, String.valueOf(value), MatchMode.END);
        }
    }, CONTAINING("containing") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.ilike(name, String.valueOf(value), MatchMode.ANYWHERE);
        }
    }, EQUALS("=") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            Criterion criterion;
            if (value instanceof Collection) {
                criterion = Restrictions.in(name, (Collection) value);
            } else if (value.getClass().isArray()) {
                criterion = Restrictions.in(name, (Object[]) value);
            } else {
                criterion = Restrictions.eq(name, value);
            }
            return criterion;
        }
    }, NOT_EQUALS("!=") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.ne(name, value);
        }
    }, BETWEEN("between") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            Criterion criterion;
            if (value instanceof Collection) {
                Collection collection = (Collection) value;
                if (collection.size() != 2)
                    throw new IllegalArgumentException("Between expression should have a collection/array of values with just two entries.");

                Iterator iterator = collection.iterator();
                criterion = Restrictions.between(name, iterator.next(), iterator.next());
            } else if (value.getClass().isArray()) {
                Object[] array = (Object[]) value;
                if (array.length != 2)
                    throw new IllegalArgumentException("Between expression should have a array/array of values with just two entries.");

                criterion = Restrictions.between(name, array[0], array[1]);
            } else {
                throw new IllegalArgumentException("Between expression should have a collection/array of values with just two entries.");
            }
            return criterion;
        }
    }, LESS_THAN("<") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.lt(name, value);
        }
    }, LESS_THAN_OR_EQUALS("<=") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.le(name, value);
        }
    }, GREATER_THAN(">") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.gt(name, value);
        }
    }, GREATER_THAN_OR_EQUALS(">=") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.ge(name, value);
        }
    }, NULL("null") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.isNull(name);
        }
    }, EMPTY("") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            return Restrictions.eq(name, "");
        }
    }, LIKE("like") {
        @Override
        public Criterion criterion(String name, Object value) {
            return Restrictions.like(name, value);
        }
    }, IN("in") {
        @Override
        public Criterion criterion(String name, Object value) {
            if (value instanceof Collection) {
                return Restrictions.in(name, (Collection) value);
            }
            throw new RuntimeException("Expected Collection type but found value of type " + value.getClass());
        }
    }, NULL_OR_EMPTY ("nullorempty") {
        @Override
        public Criterion criterion(final String name, final Object value) {
            Criterion emptyValueCriterion = Restrictions.eq(name, "");
            Criterion nullValueCriterion = Restrictions.isNull(name);
            return Restrictions.or(emptyValueCriterion, nullValueCriterion);
        }
    };


    static Map<String, Type> nameVsType = new HashMap<>();

    static {
        for (Type type : Type.values()) {
            nameVsType.put(type.getName(), type);
        }
    }


    public static Type valueFor(String typeName) {
        return nameVsType.get(typeName.toLowerCase());

    }

    private String name;

    Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    //    TODO remove null for both methods
    @Override
    public Criterion criterion(String name, Object value) {
        return null;
    }
}
