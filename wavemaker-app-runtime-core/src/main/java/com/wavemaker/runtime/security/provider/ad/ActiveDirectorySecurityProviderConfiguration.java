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

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
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

import com.wavemaker.app.security.models.config.ad.ActiveDirectoryProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.roles.RuntimeDatabaseRoleMappingConfig;

@Configuration
@Conditional({SecurityEnabledCondition.class, ActiveDirectorySecurityProviderCondition.class})
public class ActiveDirectorySecurityProviderConfiguration {

    @Bean(name = "adAuthProvider")
    public AuthenticationProvider adAuthProvider(ActiveDirectoryAuthoritiesPopulator activeDirectoryAuthoritiesPopulator,
                                                 ActiveDirectoryProviderConfig activeDirectoryProviderConfig) {
        ActiveDirectoryAuthenticationProvider activeDirectoryAuthenticationProvider = new ActiveDirectoryAuthenticationProvider(
            activeDirectoryProviderConfig.getDomain(), activeDirectoryProviderConfig.getUrl(), activeDirectoryProviderConfig.getRootDn());
        activeDirectoryAuthenticationProvider.setUserSearchPattern(activeDirectoryProviderConfig.getUserSearchPattern());
        activeDirectoryAuthenticationProvider.setAuthoritiesMapper(simpleAuthorityMapper());
        activeDirectoryAuthenticationProvider.setUserDetailsContextMapper(userDetailsContextMapper());
        activeDirectoryAuthenticationProvider.setAuthoritiesPopulator(activeDirectoryAuthoritiesPopulator);
        return activeDirectoryAuthenticationProvider;
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
    @Conditional(NoAuthoritiesPopulatorCondition.class)
    public ActiveDirectoryAuthoritiesPopulator noAuthoritiesPopulator() {
        return new NoAuthoritiesPopulator();
    }

    @Bean(name = "adAuthoritiesPopulator")
    @Conditional(DefaultActiveDirectoryAuthoritiesPopulatorCondition.class)
    public ActiveDirectoryAuthoritiesPopulator adAuthoritiesPopulator(Environment environment) {
        DefaultActiveDirectoryAuthoritiesPopulator defaultActiveDirectoryAuthoritiesPopulator = new DefaultActiveDirectoryAuthoritiesPopulator();
        defaultActiveDirectoryAuthoritiesPopulator.setGroupRoleAttribute(environment.getProperty("security.providers.ad.groupRoleAttribute"));
        return defaultActiveDirectoryAuthoritiesPopulator;
    }

    @Bean(name = "adAuthoritiesPopulator")
    @Conditional(DatabaseActiveDirectoryAuthoritiesPopulatorCondition.class)
    public ActiveDirectoryAuthoritiesPopulator activeDirectoryDatabaseAuthoritiesPopulator(ApplicationContext applicationContext,
                                                                                           RuntimeDatabaseRoleMappingConfig runtimeDatabaseRoleMappingConfig) {
        ActiveDirectoryDatabaseAuthoritiesPopulator activeDirectoryDatabaseAuthoritiesPopulator = new ActiveDirectoryDatabaseAuthoritiesPopulator();
        activeDirectoryDatabaseAuthoritiesPopulator.setAuthoritiesProvider(defaultAuthoritiesProviderImpl(applicationContext, runtimeDatabaseRoleMappingConfig));
        return activeDirectoryDatabaseAuthoritiesPopulator;
    }

    @Bean(name = "defaultAuthoritiesProvider")
    @Conditional(DatabaseActiveDirectoryAuthoritiesPopulatorCondition.class)
    public AuthoritiesProvider defaultAuthoritiesProviderImpl(ApplicationContext applicationContext,
                                                              RuntimeDatabaseRoleMappingConfig runtimeDatabaseRoleMappingConfig) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Objects.equals(runtimeDatabaseRoleMappingConfig.getQueryType(), RoleQueryType.HQL));
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(runtimeDatabaseRoleMappingConfig.getRolesByUsernameQuery());
        String roleModel = runtimeDatabaseRoleMappingConfig.getModelName();
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(roleModel + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(roleModel + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "runtimeDatabaseRoleMappingConfig")
    @Conditional(DatabaseActiveDirectoryAuthoritiesPopulatorCondition.class)
    @ConfigurationProperties("security.providers.ad.database")
    public RuntimeDatabaseRoleMappingConfig runtimeDatabaseRoleMappingConfig() {
        return new RuntimeDatabaseRoleMappingConfig();
    }

    @Bean(name = "activeDirectoryProviderConfig")
    public ActiveDirectoryProviderConfig activeDirectoryProviderConfig() {
        return new ActiveDirectoryProviderConfig();
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler securityContextLogoutHandler,
                                     LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
