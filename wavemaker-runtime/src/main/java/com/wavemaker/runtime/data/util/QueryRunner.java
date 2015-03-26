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

package com.wavemaker.runtime.data.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.hql.ast.QuerySyntaxException;

import com.wavemaker.common.util.ClassLoaderUtils;
import com.wavemaker.common.util.StringUtils;
import com.wavemaker.common.util.TypeConversionUtils;
import com.wavemaker.runtime.data.DataServiceManager;
import com.wavemaker.runtime.data.DataServiceQueryException;
import com.wavemaker.runtime.data.DefaultTaskManager;
import com.wavemaker.runtime.data.Task;
import com.wavemaker.runtime.service.PagingOptions;

/**
 * @author Simon Toens
 */
public class QueryRunner {

    private static final Task runQueryTask;

    private static final Task checkQueryTask;
    static {
        DefaultTaskManager taskMgr = DefaultTaskManager.getInstance();
        runQueryTask = taskMgr.getRunQueryTask();
        checkQueryTask = taskMgr.getCheckQueryTask();
    }

    private final DataServiceManager mgr;

    private final Map<String, Object> bindParameters = new HashMap<String, Object>();

    private final Map<String, Class<?>> bindParameterTypes = new HashMap<String, Class<?>>();// salesforce

    private Long maxResults = null;

    public QueryRunner(DataServiceManager mgr) {
        this.mgr = mgr;
    }

    // value has not been split yet
    @SuppressWarnings("unused")
    public void addBindParameters(List<String> names, List<String> types, String value, List<Boolean> isList) {

        List<String> values = new ArrayList<String>(names.size());
        if (value == null) {
            for (String s : names) {
                values.add(null);
            }
        } else {
            values = StringUtils.split(value);
        }

        for (int i = 0; i < names.size(); i++) {
            addBindParameter(names.get(i), types.get(i), values.get(i), isList.get(i));
        }
    }

    public void addBindParameter(String name, String type, String value) {
        addBindParameter(name, type, value, false);
    }

    public void addBindParameter(String name, String type, String value, boolean isList) {
        addBindParameter(name, ClassLoaderUtils.loadClass(type, false), value, isList);
    }

    public void addBindParameter(String name, Class<?> type, String value) {
        addBindParameter(name, type, value, false);
    }

    public void addBindParameter(String name, Class<?> type, String value, boolean isList) {
        Object o = TypeConversionUtils.fromString(type, value, isList);
        this.bindParameters.put(name, o);
        this.bindParameterTypes.put(name, type);
    }

    public void setMaxResults(Long maxResults) {
        this.maxResults = maxResults;
    }

    public void check(String query) {

        Object[] args = setupTaskArgs(query, false);

        try {

            this.mgr.invoke(checkQueryTask, args);

        } catch (RuntimeException ex) {
            ex = DataServiceUtils.unwrap(ex);
            if (ex instanceof QuerySyntaxException || ex instanceof DataServiceQueryException) {
                // ignore connection errors
                throw ex;
            }
        } catch (Exception ex1) {

        }
    }

    public Object run(String query) {

        try {

            Object[] args = setupTaskArgs(query, true);

            Object rtn = this.mgr.invoke(runQueryTask, args);

            SystemUtils.clientPrepare();

            return rtn;

        } finally {
            this.bindParameters.clear();
        }
    }

    // salesforce
    public Object run(String query, boolean named) {

        try {

            Object[] args = setupTaskArgs(query, true);

            Object rtn;
            rtn = this.mgr.invoke(runQueryTask, this.bindParameterTypes, named, args);

            SystemUtils.clientPrepare();

            return rtn;

        } finally {
            this.bindParameters.clear();
            this.bindParameterTypes.clear();
        }
    }

    public void dispose() {
        this.mgr.dispose();
    }

    private Object[] setupTaskArgs(String query, boolean includePagingOptions) {

        int size = this.bindParameters.size() * 2 + 1;

        if (includePagingOptions) {
            size++;
        }

        Object[] args = new Object[size];

        args[0] = query;

        int i = 1;

        for (String paramName : this.bindParameters.keySet()) {
            args[i] = paramName;
            args[i + 1] = this.bindParameters.get(paramName);
            i += 2;
        }

        if (includePagingOptions) {
            PagingOptions pagingOptions = new PagingOptions();
            pagingOptions.setMaxResults(this.maxResults);
            args[i] = pagingOptions;
        }

        return args;
    }
}
