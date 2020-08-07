/**
 * Copyright © 2013 - 2017 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.wavemaker.runtime.data.interceptors;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

public class WMEntityInterceptor extends EmptyInterceptor {
    private List<Interceptor> interceptors;

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
        boolean result = false;
        for (Interceptor interceptor : interceptors) {
            result |= interceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        }
        return result;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        boolean result = false;
        for (Interceptor interceptor : interceptors) {
            result |= interceptor.onSave(entity, id, state, propertyNames, types);
        }
        return result;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        for (Interceptor interceptor : interceptors) {
            interceptor.onDelete(entity, id, state, propertyNames, types);
        }
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean result = false;
        for (Interceptor interceptor : interceptors) {
            result |= interceptor.onLoad(entity, id, state, propertyNames, types);
        }
        return result;
    }

    @Override
    public String onPrepareStatement(String sql) {
        for (Interceptor interceptor : interceptors) {
            interceptor.onPrepareStatement(sql);
        }
        return sql;
    }

    @Override
    public void postFlush(Iterator entities) {
        for (Interceptor interceptor : interceptors) {
            interceptor.postFlush(entities);
        }

    }

    @Override
    public void preFlush(Iterator entities) {
        for (Interceptor interceptor : interceptors) {
            interceptor.preFlush(entities);
        }
    }

    @Override
    public Boolean isTransient(Object entity) {
        return super.isTransient(entity);
    }

    @Override
    public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
        return super.instantiate(entityName, entityMode, id);
    }

    @Override
    public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public String getEntityName(Object object) {
        return super.getEntityName(object);
    }

    @Override
    public Object getEntity(String entityName, Serializable id) {
        return super.getEntity(entityName, id);
    }

    @Override
    public void afterTransactionBegin(Transaction tx) {
        for (Interceptor interceptor : interceptors) {
            interceptor.afterTransactionBegin(tx);
        }
    }

    @Override
    public void afterTransactionCompletion(Transaction tx) {
        for (Interceptor interceptor : interceptors) {
            interceptor.afterTransactionCompletion(tx);
        }
    }

    @Override
    public void beforeTransactionCompletion(Transaction tx) {
        for (Interceptor interceptor : interceptors) {
            interceptor.beforeTransactionCompletion(tx);
        }
    }

    @Override
    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
        for (Interceptor interceptor : interceptors) {
            interceptor.onCollectionRemove(collection, key);
        }
    }

    @Override
    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
        for (Interceptor interceptor : interceptors) {
            interceptor.onCollectionRecreate(collection, key);
        }
    }

    @Override
    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        for (Interceptor interceptor : interceptors) {
            interceptor.onCollectionUpdate(collection, key);
        }
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }
}
