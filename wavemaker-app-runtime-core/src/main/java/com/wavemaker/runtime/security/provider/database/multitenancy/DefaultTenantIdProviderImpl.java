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
package com.wavemaker.runtime.security.provider.database.multitenancy;

import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.Session;

import com.wavemaker.runtime.security.core.AuthenticationContext;
import com.wavemaker.runtime.security.core.TenantIdProvider;
import com.wavemaker.runtime.security.provider.database.AbstractDatabaseSupport;

public class DefaultTenantIdProviderImpl extends AbstractDatabaseSupport implements TenantIdProvider {

    private static final String USERNAME = "username";
    private static final String COLON_USERNAME = ":username";
    private static final String Q_MARK = "?";

    private String tenantIdByUsernameQuery = "SELECT tenantId FROM User WHERE username = ?";
    private boolean tenantIdByQuery;
    private static final String LOGGED_IN_USERNAME = ":LOGGED_IN_USERNAME";

    @PostConstruct
    protected void init() {
        if (tenantIdByUsernameQuery.contains(LOGGED_IN_USERNAME)) {
            tenantIdByUsernameQuery = tenantIdByUsernameQuery.replace(LOGGED_IN_USERNAME, COLON_USERNAME);
        }
        if (tenantIdByUsernameQuery.contains(Q_MARK)) {
            tenantIdByUsernameQuery = tenantIdByUsernameQuery.replace(Q_MARK, COLON_USERNAME);
        }
    }

    @Override
    public Object loadTenantId(AuthenticationContext authenticationContext) {
        return getTransactionTemplate()
                .execute(status -> getHibernateTemplate().execute(session -> getTenantId(session, authenticationContext.getUsername())));
    }

    public String getTenantIdByUsernameQuery() {
        return tenantIdByUsernameQuery;
    }

    public void setTenantIdByUsernameQuery(String tenantIdByUsernameQuery) {
        this.tenantIdByUsernameQuery = tenantIdByUsernameQuery;
    }

    public boolean isTenantIdByQuery() {
        return tenantIdByQuery;
    }

    public void setTenantIdByQuery(boolean tenantIdByQuery) {
        this.tenantIdByQuery = tenantIdByQuery;
    }

    public Object loadUserTenantId(final String username) {
        return getTransactionTemplate()
                .execute(status -> getHibernateTemplate().execute(session -> getTenantId(session, username)));
    }

    private Object getTenantId(final Session session, final String username) {
        String tenantIdByUsernameQuery = getTenantIdByUsernameQuery();
        if (!isHql()) {
            return getTenantIdByNativeSql(session, tenantIdByUsernameQuery, username);
        } else {
            return getTenantIdByHQL(session, tenantIdByUsernameQuery, username);
        }
    }

    private Object getTenantIdByHQL(
            Session session, String tenantIdByUsernameQuery, String username) {
        final List list = session.createQuery(tenantIdByUsernameQuery).setParameter(USERNAME, username).list();
        return getTenantIdObject(list);
    }

    private Object getTenantIdByNativeSql(
            Session session, String tenantIdByUsernameQuery, String username) {
        final List list = session.createNativeQuery(tenantIdByUsernameQuery).setParameter(USERNAME, username).list();
        return getTenantIdObject(list);
    }

    private Object getTenantIdObject(List<Object> list) {
        Object tenantId = null;
        Object o = list.get(0);
        if (o instanceof Object[]) {
            Object[] result = (Object[]) o;
            tenantId = result[0];
        } else {
            tenantId = o;
        }
        return tenantId;
    }
}
