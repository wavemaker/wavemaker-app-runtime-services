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
package com.wavemaker.runtime.data.interceptors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.persistence.Table;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.runtime.data.exception.EntityNotAuthorizedException;
import com.wavemaker.runtime.data.exception.EntityNotFoundException;
import com.wavemaker.runtime.security.SecurityService;

public class MultiTenantDiscriminatorInterceptor extends EmptyInterceptor {

    private String tenantIdFieldName;
    private String tenantIdFieldType;
    private HibernateTemplate template;
    private List<String> excludedTables;

    @Autowired
    private SecurityService securityService;

    public void setTemplate(HibernateTemplate template) {
        this.template = template;
    }

    public void setTenantIdFieldName(String tenantIdFieldName) {
        this.tenantIdFieldName = tenantIdFieldName;
    }

    public void setExcludedTables(List<String> excludedTables) {
        this.excludedTables = excludedTables;
    }

    public void setTenantIdFieldType(String tenantIdFieldType) {
        this.tenantIdFieldType = tenantIdFieldType;
    }

    @Override
    public String onPrepareStatement(String sql) {
        if (securityService.isAuthenticated()) {
            Object tenantId = securityService.getTenantIdOfUser();
            if (tenantId != null) {
                Objects.requireNonNull(template.getSessionFactory()).getCurrentSession().
                        enableFilter("tenantIdFilter").
                        setParameter("tenantId", tenantId);
            }
        }
        return super.onPrepareStatement(sql);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        int indexOfTenantIdColumn = getIndexOfTenantIdColumn(propertyNames);
        if (indexOfTenantIdColumn != -1 && !isTableExcluded(entity) && checkTenantIdType(indexOfTenantIdColumn, types)) {
            Object tenantId = previousState[indexOfTenantIdColumn];
            if (!Objects.equals(tenantId, securityService.getTenantIdOfUser())) {
                throw new EntityNotAuthorizedException(MessageResource.create("com.wavemaker.runtime.entity.not.authorized"));
            } else {
                currentState[indexOfTenantIdColumn] = securityService.getTenantIdOfUser();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        int indexOfTenantIdColumn = getIndexOfTenantIdColumn(propertyNames);
        if (indexOfTenantIdColumn != -1 && !isTableExcluded(entity) && checkTenantIdType(indexOfTenantIdColumn, types)) {
            state[indexOfTenantIdColumn] = securityService.getTenantIdOfUser();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (!canCurrentUserAccess(state, propertyNames, entity, types)) {
            throw new EntityNotAuthorizedException(MessageResource.create("com.wavemaker.runtime.entity.not.authorized"));
        }
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (securityService.isAuthenticated()) {
            if (securityService.getTenantIdOfUser() != null) {
                if (!canCurrentUserAccess(state, propertyNames, entity, types)) {
                    throw new EntityNotFoundException();
                }
            }
        }
        return false;
    }

    private boolean canCurrentUserAccess(Object[] state, String[] propertyNames, Object entity, Type[] types) {
        int indexOfTenantIdColumn = getIndexOfTenantIdColumn(propertyNames);
        if (indexOfTenantIdColumn != -1 && !isTableExcluded(entity) && checkTenantIdType(indexOfTenantIdColumn, types)) {
            Object tenantId = state[indexOfTenantIdColumn];
            if (!Objects.equals(tenantId, securityService.getTenantIdOfUser())) {
                return false;
            }
        }
        return true;
    }

    private int getIndexOfTenantIdColumn(String[] propertyNames) {
        for (int index = 0; index < propertyNames.length; index++) {
            if (propertyNames[index].equals(tenantIdFieldName)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isTableExcluded(Object entity) {
        String tableName = entity.getClass().getAnnotation(Table.class).name();
        return excludedTables.contains("`" + tableName + "`");
    }

    private boolean checkTenantIdType(int indexOfTenantIdColumn, Type[] types) {
        return types[indexOfTenantIdColumn].getName().equals(tenantIdFieldType);
    }
}
