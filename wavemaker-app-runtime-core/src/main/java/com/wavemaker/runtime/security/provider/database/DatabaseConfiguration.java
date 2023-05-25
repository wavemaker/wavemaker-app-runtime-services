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

package com.wavemaker.runtime.security.provider.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.runtime.security.csrf.WMCsrfLogoutHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.database.users.DefaultUserProviderImpl;

@Configuration
@Conditional(DatabaseProviderCondition.class)
public class DatabaseConfiguration {

    @Value("${security.providers.database.rolesByUsernameQuery}")
    private String rolesByUsernameQuery;
    @Value("${security.providers.database.usersByUsernameQuery}")
    private String usersByUsernameQuery;
    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name = "databaseAuthenticationProvider")
    public DaoAuthenticationProvider getDaoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(getDatabaseUserDetailsService());
        authenticationProvider.setPasswordEncoder(getNoOpPasswordEncoder());
        return authenticationProvider;
    }

    //TODO NoOpPasswordEncoder is deprecated
    @Bean
    public PasswordEncoder getNoOpPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean(name = "jdbcDaoImpl")
    public DatabaseUserDetailsService getDatabaseUserDetailsService() {
        DatabaseUserDetailsService detailsService = new DatabaseUserDetailsService();
        detailsService.setAuthoritiesProvider(getDefaultAuthoritiesProviderImpl());
        detailsService.setUserProvider(getDefaultUserProviderImpl());
        return detailsService;
    }

    @Bean(name = "defaultUserProvider")
    public DefaultUserProviderImpl getDefaultUserProviderImpl() {
        DefaultUserProviderImpl defaultUserProvider = new DefaultUserProviderImpl();
        defaultUserProvider.setHql(true);
        defaultUserProvider.setUsersByUsernameQuery(usersByUsernameQuery);
        defaultUserProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(environment
            .getProperty("security.providers.database.modelName") + "Template"));
        defaultUserProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(environment
            .getProperty("security.providers.database.modelName") + "TransactionManager"));
        return defaultUserProvider;
    }

    @Bean(name = "defaultAuthoritiesProvider")
    public DefaultAuthoritiesProviderImpl getDefaultAuthoritiesProviderImpl() {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Boolean.parseBoolean(environment.getProperty("security.providers.database.isHQL")));
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(rolesByUsernameQuery);
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(environment
            .getProperty("security.providers.database.modelName") + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(environment
            .getProperty("security.providers.database.modelName") + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(SimpleUrlLogoutSuccessHandler logoutSuccessHandler, SecurityContextLogoutHandler securityContextLogoutHandler,
                                     WMCsrfLogoutHandler wmCsrfLogoutHandler, PersistentTokenBasedRememberMeServices rememberMeServices) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler, rememberMeServices);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
