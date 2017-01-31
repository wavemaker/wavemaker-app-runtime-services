/**
 * Copyright © 2013 - 2017 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.security.provider.database.authorities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.wavemaker.runtime.security.provider.database.AbstractDatabaseSupport;

/**
 * Created by ArjunSahasranam on 11/3/16.
 */
public class DefaultAuthoritiesProviderImpl extends AbstractDatabaseSupport implements AuthoritiesProvider {

    private String authoritiesByUsernameQuery = "SELECT userid, role FROM User WHERE username = ?";
    private String rolePrefix = "ROLE_";
    private boolean rolesByQuery = false;
    private static final String LOGGED_IN_USERNAME = ":LOGGED_IN_USERNAME";


    @PostConstruct
    protected void init() {
        if (authoritiesByUsernameQuery.contains(LOGGED_IN_USERNAME)) {
            authoritiesByUsernameQuery = authoritiesByUsernameQuery.replace(LOGGED_IN_USERNAME, "?");
        }
    }

    public String getAuthoritiesByUsernameQuery() {
        return authoritiesByUsernameQuery;
    }

    public void setAuthoritiesByUsernameQuery(final String authoritiesByUsernameQuery) {
        this.authoritiesByUsernameQuery = authoritiesByUsernameQuery;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(final String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    public boolean isRolesByQuery() {
        return rolesByQuery;
    }

    public void setRolesByQuery(final boolean rolesByQuery) {
        this.rolesByQuery = rolesByQuery;
    }

    public List<GrantedAuthority> loadUserAuthorities(final String username) {
        final List<GrantedAuthority> execute = getTransactionTemplate().execute(
                new TransactionCallback<List<GrantedAuthority>>() {
                    @Override
                    public List<GrantedAuthority> doInTransaction(final TransactionStatus status) {
                        final List<GrantedAuthority> grantedAuthorities = getHibernateTemplate()
                                .execute(new HibernateCallback<List<GrantedAuthority>>() {
                                    @Override
                                    public List<GrantedAuthority> doInHibernate(Session session) {
                                        return getGrantedAuthorities(session, username);
                                    }
                                });
                        return grantedAuthorities;
                    }
                });
        return execute;
    }

    private List<GrantedAuthority> getGrantedAuthorities(final Session session, final String username) {
        String authoritiesByUsernameQuery = getAuthoritiesByUsernameQuery();
        authoritiesByUsernameQuery = authoritiesByUsernameQuery.replace("?", "\'" + username + "\'");
        if (!isHql()) {
            return getGrantedAuthoritiesByNativeSql(session, authoritiesByUsernameQuery);
        } else {
            return getGrantedAuthoritiesByHQL(session, authoritiesByUsernameQuery);
        }
    }

    private List<GrantedAuthority> getGrantedAuthoritiesByHQL(Session session, String authoritiesByUsernameQuery) {
        final Query query = session.createQuery(authoritiesByUsernameQuery);
        final List list = query.list();
        return getAuthorities(list);
    }

    private List<GrantedAuthority> getGrantedAuthoritiesByNativeSql(
            final Session session, final String authoritiesByUsernameQuery) {
        final Query query = session.createSQLQuery(authoritiesByUsernameQuery);
        final List list = query.list();
        return getAuthorities(list);
    }

    private List<GrantedAuthority> getAuthorities(List<Object> content) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (content.size() > 0) {
            for (Object o : content) {
                Object role = null;
                if (o instanceof Object[]) {
                    Object[] result = (Object[]) o;
                    if (result.length == 1) {
                        role = String.valueOf(result[0]);
                    } else {
                        role = String.valueOf(result[1]);
                    }
                } else {
                    role = o;
                }
                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(getRolePrefix() + role);
                grantedAuthorities.add(simpleGrantedAuthority);
            }
            return grantedAuthorities;
        }
        return grantedAuthorities;
    }

}
