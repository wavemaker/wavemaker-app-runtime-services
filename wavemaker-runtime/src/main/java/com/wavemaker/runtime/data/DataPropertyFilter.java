/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.data;

import java.util.Collection;
import java.util.HashSet;

import org.hibernate.Hibernate;

import com.wavemaker.json.PropertyFilter;

/**
 * @author Simon Toens
 */
public class DataPropertyFilter implements PropertyFilter {

    public static DataPropertyFilter getInstance() {
        return INSTANCE;
    }

    private static final DataPropertyFilter INSTANCE = new DataPropertyFilter();

    private static final Collection<String> ALWAYS_FILTERED = new HashSet<String>(1);
    static {
        ALWAYS_FILTERED.add("hibernateLazyInitializer");
    }

    protected DataPropertyFilter() {

    }

    @Override
    public boolean filter(Object source, String name, Object value) {

        if (ALWAYS_FILTERED.contains(name)) {
            return true;
        }

        return !Hibernate.isInitialized(value);
    }

}
