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

package com.wavemaker.runtime.security.provider.ad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional(ActiveDirectoryProviderCondition.class)
public class ActiveDirectoryConfiguration {
    @Value("${security.providers.ad.domain}")
    private String domain;

    @Value("${security.providers.ad.url}")
    private String url;

    @Value("${security.providers.ad.rootDn}")
    private String rootDn;

    @Value("${security.providers.ad.database.rolesByUsernameQuery}")
    private String authoritiesByUsernameQuery;

    @Value("${security.providers.ad.roleProvider}")
    private String roleProvider;
    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name = "adAuthProvider")
    public ActiveDirectoryAuthenticationProvider getAdAuthProvider() {
        ActiveDirectoryAuthenticationProvider activeDirectoryAuthenticationProvider = new ActiveDirectoryAuthenticationProvider(domain, url, rootDn);
        activeDirectoryAuthenticationProvider.setUserSearchPattern(environment.getProperty("security.providers.ad.userSearchPattern"));
        activeDirectoryAuthenticationProvider.setAuthoritiesMapper(getSimpleAuthorityMapper());
        activeDirectoryAuthenticationProvider.setUserDetailsContextMapper(getUserDetailsContextMapper());
        String groupSearchDisabled = environment.getProperty("security.providers.ad.groupSearchDisabled");
        if (!groupSearchDisabled.equals(true)) {
            if (roleProvider.equals("Database")) {
                activeDirectoryAuthenticationProvider.setAuthoritiesPopulator(getActiveDirectoryDatabaseAuthoritiesPopulator());
            } else {
                activeDirectoryAuthenticationProvider.setAuthoritiesPopulator(getAdAuthoritiesPopulator());
            }
        } else {
            activeDirectoryAuthenticationProvider.setAuthoritiesPopulator(getAdAuthoritiesPopulator());
        }
        return activeDirectoryAuthenticationProvider;
    }

    @Bean(name = "adDatabaseAuthoritiesPopulator")
    @Conditional(ActiveDirectoryDatabaseAuthorityProviderCondition.class)
    public ActiveDirectoryDatabaseAuthoritiesPopulator getActiveDirectoryDatabaseAuthoritiesPopulator() {
        ActiveDirectoryDatabaseAuthoritiesPopulator activeDirectoryDatabaseAuthoritiesPopulator = new ActiveDirectoryDatabaseAuthoritiesPopulator();
        activeDirectoryDatabaseAuthoritiesPopulator.setAuthoritiesProvider(getDefaultAuthoritiesProviderImpl());
        return activeDirectoryDatabaseAuthoritiesPopulator;
    }
//    @Conditional(ActiveDirectoryDatabaseAuthorityProviderCondition.class)

    @Bean(name = "authoritiesMapper")
    public SimpleAuthorityMapper getSimpleAuthorityMapper() {
        SimpleAuthorityMapper simpleAuthorityMapper = new SimpleAuthorityMapper();
        simpleAuthorityMapper.setDefaultAuthority("ROLE_DEFAULT_NO_ROLES");
        simpleAuthorityMapper.setPrefix("ROLE_");
        simpleAuthorityMapper.setConvertToUpperCase(false);
        return simpleAuthorityMapper;
    }

    @Bean(name = "userDetailsContextMapper")
    public UserDetailsContextMapper getUserDetailsContextMapper() {
        return new LdapUserDetailsMapper();
    }

    @Bean(name = "adAuthoritiesPopulator")
    public DefaultActiveDirectoryAuthoritiesPopulator getAdAuthoritiesPopulator() {
        DefaultActiveDirectoryAuthoritiesPopulator defaultActiveDirectoryAuthoritiesPopulator = new DefaultActiveDirectoryAuthoritiesPopulator();
        defaultActiveDirectoryAuthoritiesPopulator.setGroupRoleAttribute(environment.getProperty("security.providers.ad.groupRoleAttribute"));
        return defaultActiveDirectoryAuthoritiesPopulator;
    }

    @Bean
    @Conditional(ActiveDirectoryDatabaseAuthorityProviderCondition.class)
    public DefaultAuthoritiesProviderImpl getDefaultAuthoritiesProviderImpl() {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(environment.getProperty("security.providers.ad.database.isHql", Boolean.class, false));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(authoritiesByUsernameQuery);
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(environment
            .getProperty("security.providers.ad.database.modelName") + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(environment
            .getProperty("security.providers.ad.database.modelName") + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }
//    @Conditional(ActiveDirectoryDatabaseAuthorityProviderCondition.class)

}
