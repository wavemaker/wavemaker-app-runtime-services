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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional({SecurityEnabledCondition.class, ActiveDirectoryProviderCondition.class})
public class ActiveDirectorySecurityProviderConfiguration {
    @Autowired
    private Environment environment;

    @Bean(name = "adAuthProvider")
    public AuthenticationProvider adAuthProvider(ApplicationContext applicationContext) {
        ActiveDirectoryAuthenticationProvider activeDirectoryAuthenticationProvider = new ActiveDirectoryAuthenticationProvider(
            environment.getProperty("security.providers.ad.domain"), environment.getProperty("security.providers.ad.url"),
            environment.getProperty("security.providers.ad.rootDn"));
        activeDirectoryAuthenticationProvider.setUserSearchPattern(environment.getProperty("security.providers.ad.userSearchPattern"));
        activeDirectoryAuthenticationProvider.setAuthoritiesMapper(simpleAuthorityMapper());
        activeDirectoryAuthenticationProvider.setUserDetailsContextMapper(userDetailsContextMapper());
        boolean groupSearchDisabled = environment.getProperty("security.providers.ad.groupSearchDisabled", Boolean.class);
        String roleProvider = environment.getProperty("security.providers.ad.roleProvider");
        if (!groupSearchDisabled) {
            if (roleProvider != null && roleProvider.equals("Database")) {
                activeDirectoryAuthenticationProvider.setAuthoritiesPopulator(activeDirectoryDatabaseAuthoritiesPopulator(applicationContext));
            } else {
                activeDirectoryAuthenticationProvider.setAuthoritiesPopulator(adAuthoritiesPopulator());
            }
        } else {
            activeDirectoryAuthenticationProvider.setAuthoritiesPopulator(adAuthoritiesPopulator());
        }
        return activeDirectoryAuthenticationProvider;
    }

    @Bean(name = "adAuthoritiesPopulator")
    @Conditional(ActiveDirectoryDatabaseAuthorityProviderCondition.class)
    public ActiveDirectoryAuthoritiesPopulator activeDirectoryDatabaseAuthoritiesPopulator(ApplicationContext applicationContext) {
        ActiveDirectoryDatabaseAuthoritiesPopulator activeDirectoryDatabaseAuthoritiesPopulator = new ActiveDirectoryDatabaseAuthoritiesPopulator();
        activeDirectoryDatabaseAuthoritiesPopulator.setAuthoritiesProvider(defaultAuthoritiesProviderImpl(applicationContext));
        return activeDirectoryDatabaseAuthoritiesPopulator;
    }

    @Bean(name = "authoritiesMapper")
    public GrantedAuthoritiesMapper simpleAuthorityMapper() {
        SimpleAuthorityMapper simpleAuthorityMapper = new SimpleAuthorityMapper();
        simpleAuthorityMapper.setDefaultAuthority("ROLE_DEFAULT_NO_ROLES");
        simpleAuthorityMapper.setPrefix("ROLE_");
        simpleAuthorityMapper.setConvertToUpperCase(false);
        return simpleAuthorityMapper;
    }

    @Bean(name = "userDetailsContextMapper")
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new LdapUserDetailsMapper();
    }

    @Bean(name = "adAuthoritiesPopulator")
    public ActiveDirectoryAuthoritiesPopulator adAuthoritiesPopulator() {
        DefaultActiveDirectoryAuthoritiesPopulator defaultActiveDirectoryAuthoritiesPopulator = new DefaultActiveDirectoryAuthoritiesPopulator();
        defaultActiveDirectoryAuthoritiesPopulator.setGroupRoleAttribute(environment.getProperty("security.providers.ad.groupRoleAttribute"));
        return defaultActiveDirectoryAuthoritiesPopulator;
    }

    @Bean(name = "defaultAuthoritiesProvider")
    @Conditional(ActiveDirectoryDatabaseAuthorityProviderCondition.class)
    public AuthoritiesProvider defaultAuthoritiesProviderImpl(ApplicationContext applicationContext) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(environment.getProperty("security.providers.ad.database.isHQL", Boolean.class, false));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty("security.providers.ad.database.rolesByUsernameQuery"));
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(environment
            .getProperty("security.providers.ad.database.modelName") + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(environment
            .getProperty("security.providers.ad.database.modelName") + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler securityContextLogoutHandler,
                                     LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
