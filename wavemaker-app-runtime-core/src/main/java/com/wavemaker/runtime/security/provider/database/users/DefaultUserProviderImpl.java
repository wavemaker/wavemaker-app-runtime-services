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
package com.wavemaker.runtime.security.provider.database.users;

import java.math.BigInteger;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.hibernate.Session;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.wavemaker.runtime.security.WMUser;
import com.wavemaker.runtime.security.WMUserDetails;
import com.wavemaker.runtime.security.provider.database.AbstractDatabaseSupport;

/**
 * Created by ArjunSahasranam on 11/3/16.
 */
public class DefaultUserProviderImpl extends AbstractDatabaseSupport implements UserProvider {

    private static final String Q_MARK = "?";
    private static final String COLON_USERNAME = ":username";
    private static final String USERNAME = "username";
    private String usersByUsernameQuery = "SELECT userid, password, 1, username FROM User WHERE username = ?";

    public String getUsersByUsernameQuery() {
        return usersByUsernameQuery;
    }

    public void setUsersByUsernameQuery(final String usersByUsernameQuery) {
        this.usersByUsernameQuery = usersByUsernameQuery;
    }

    @Override
    public UserDetails loadUser(final String username) {
        return getTransactionTemplate()
            .execute(status -> getHibernateTemplate().execute(session -> getWmUser(session, username)));
    }

    @Override
    public UserDetails createUserDetails(
        String username, UserDetails userDetails,
        List<GrantedAuthority> combinedAuthorities) {
        WMUserDetails wmUserDetails = (WMUserDetails) userDetails;
        return new WMUser(wmUserDetails.getUserId(), wmUserDetails.getUsername(), wmUserDetails.getPassword(),
            wmUserDetails.getUserLongName(),
            wmUserDetails.getTenantId(), wmUserDetails.isEnabled(), true, true, true, combinedAuthorities,
            wmUserDetails.getLoginTime());
    }

    private WMUser getWmUser(final Session session, final String username) {
        String usersByUsernameQuery = getUsersByUsernameQuery();
        if (isHql()) {
            final List list = session.createQuery(usersByUsernameQuery).setParameter(USERNAME, username).list();
            return getWmUser(list);
        } else {
            final List list = session.createNativeQuery(usersByUsernameQuery).setParameter(USERNAME, username).list();
            return getWmUser(list);
        }
    }

    private WMUser getWmUser(List<Object> content) {
        if (!content.isEmpty()) {
            Object[] resultMap = (Object[]) content.get(0);
            String userId = String.valueOf(resultMap[0]);
            String password = String.valueOf(resultMap[1]);
            // MYSQL returns BigInteger. Enabled column should be introduced or "1" should be removed.
            int enabled = 0;
            final Object column3 = resultMap[2];
            if (column3 instanceof Integer) {
                enabled = (Integer) column3;
            } else if (column3 instanceof BigInteger bigInteger) {
                enabled = bigInteger.intValue();
            }
            String userName = String.valueOf(resultMap[3]);
            int tenantId = -1;
            long loginTime = System.currentTimeMillis();
            boolean isEnabled = enabled == 1 ? true : false;
            return new WMUser(userId, userName, password, userName, tenantId,
                isEnabled, true, true,
                true, AuthorityUtils.NO_AUTHORITIES, loginTime);
        }
        return null;
    }

    @PostConstruct
    private void init() {
        String usersByUsernameQuery = getUsersByUsernameQuery();
        if (usersByUsernameQuery.contains(Q_MARK)) {
            setUsersByUsernameQuery(usersByUsernameQuery.replace(Q_MARK, COLON_USERNAME));
        }
    }
}
