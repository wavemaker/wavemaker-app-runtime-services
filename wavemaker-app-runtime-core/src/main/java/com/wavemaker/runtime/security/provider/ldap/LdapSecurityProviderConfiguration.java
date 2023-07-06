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

package com.wavemaker.runtime.security.provider.ldap;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.config.ldap.LdapProviderConfig;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional({SecurityEnabledCondition.class, LdapProviderCondition.class})
public class LdapSecurityProviderConfiguration {

    @Autowired(required = false)
    private PersistentTokenBasedRememberMeServices rememberMeServices;

    @Bean(name = "contextSource")
    public ContextSource contextSource(LdapProviderConfig ldapProviderConfig) {
        AbstractContextSource abstractContextSource = new WMSpringSecurityContextSource(ldapProviderConfig.getUrl());
        String managerDn = ldapProviderConfig.getManagerDn();
        if (StringUtils.isNotBlank(managerDn)) {
            abstractContextSource.setUserDn(managerDn);
            abstractContextSource.setPassword(ldapProviderConfig.getManagerPassword());
        }
        return abstractContextSource;
    }

    @Bean(name = "defaultAuthoritiesProvider")
    @Conditional(LdapDatabaseAuthoritiesProviderCondition.class)
    public AuthoritiesProvider defaultAuthoritiesProvider(ApplicationContext applicationContext, LdapProviderConfig ldapProviderConfig) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(ldapProviderConfig.getQueryType().equals("HQL"));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(ldapProviderConfig.getRoleQuery());
        String modelName = ldapProviderConfig.getRoleModel();
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(modelName + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(modelName + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "ldapAuthoritiesPopulator")
    @Conditional(LdapDatabaseAuthoritiesProviderCondition.class)
    public LdapAuthoritiesPopulator ldapDatabaseAuthoritiesPopulator(AuthoritiesProvider authoritiesProvider) {
        return new LdapDatabaseAuthoritiesPopulator(authoritiesProvider);
    }

    @Bean(name = "ldapAuthoritiesPopulator")
    @Conditional(LdapNullAuthProviderCondition.class)
    public LdapAuthoritiesPopulator ldapNullAuthoritiesPopulator() {
        return new NullLdapAuthoritiesPopulator();
    }

    @Bean(name = "ldapAuthoritiesPopulator")
    @Conditional(LdapAuthoritiesProviderCondition.class)
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator(LdapProviderConfig ldapProviderConfig) {
        DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator =
            new DefaultLdapAuthoritiesPopulator(contextSource(ldapProviderConfig), ldapProviderConfig.getGroupSearchBase());
        ldapAuthoritiesPopulator.setGroupSearchFilter(ldapProviderConfig.getGroupSearchFilter());
        ldapAuthoritiesPopulator.setGroupRoleAttribute(ldapProviderConfig.getGroupRoleAttribute());
        return ldapAuthoritiesPopulator;
    }

    @Bean(name = "userSearch")
    public LdapUserSearch userSearch(LdapProviderConfig ldapProviderConfig) {
        return new FilterBasedLdapUserSearch(Objects.requireNonNull(ldapProviderConfig.getGroupSearchBase()),
            Objects.requireNonNull(ldapProviderConfig.getUserDnPattern()),
            (BaseLdapPathContextSource) contextSource(ldapProviderConfig));
    }

    @Bean(name = "bindAuthenticator")
    public LdapAuthenticator bindAuthenticator(LdapProviderConfig ldapProviderConfig) {
        BindAuthenticator bindAuthenticator = new BindAuthenticator((BaseLdapPathContextSource) contextSource(ldapProviderConfig));
        bindAuthenticator.setUserSearch(userSearch(ldapProviderConfig));
        String[] userDnPatterns = new String[]{ldapProviderConfig.getUserDnPattern()};
        bindAuthenticator.setUserDnPatterns(userDnPatterns);
        return bindAuthenticator;
    }

    @Bean(name = "ldapAuthenticationProvider")
    public AuthenticationProvider ldapAuthenticationProvider(ApplicationContext applicationContext, LdapProviderConfig ldapProviderConfig) {
        LdapAuthenticationProvider ldapAuthenticationProvider;
        boolean groupSearchDisabled = ldapProviderConfig.isGroupSearchDisabled();
        String roleProvider = ldapProviderConfig.getRoleProvider();
        if (!groupSearchDisabled && roleProvider != null) {
            if (roleProvider.equals("LDAP")) {
                ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator(ldapProviderConfig), ldapAuthoritiesPopulator(ldapProviderConfig));
                ldapAuthenticationProvider.setAuthoritiesMapper(authoritiesMapper());
                return ldapAuthenticationProvider;
            } else if (roleProvider.equals("Database")) {
                ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator(ldapProviderConfig), ldapDatabaseAuthoritiesPopulator(
                    defaultAuthoritiesProvider(applicationContext,ldapProviderConfig)));
                ldapAuthenticationProvider.setAuthoritiesMapper(authoritiesMapper());
                return ldapAuthenticationProvider;
            }
        }
        ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator(ldapProviderConfig), ldapNullAuthoritiesPopulator());
        ldapAuthenticationProvider.setAuthoritiesMapper(authoritiesMapper());
        return ldapAuthenticationProvider;
    }

    @Bean(name = "authoritiesMapper")
    public GrantedAuthoritiesMapper authoritiesMapper() {
        SimpleAuthorityMapper simpleAuthorityMapper = new SimpleAuthorityMapper();
        simpleAuthorityMapper.setDefaultAuthority("ROLE_DEFAULT_NO_ROLES");
        simpleAuthorityMapper.setPrefix("ROLE_");
        simpleAuthorityMapper.setConvertToUpperCase(false);
        return simpleAuthorityMapper;
    }

    @Bean(name = "ldapUserDetailsService")
    public UserDetailsService ldapUserDetailsService(ApplicationContext applicationContext, LdapProviderConfig ldapProviderConfig) {
        boolean groupSearchDisabled = ldapProviderConfig.isGroupSearchDisabled();
        String roleProvider = ldapProviderConfig.getRoleProvider();
        if (!groupSearchDisabled && roleProvider != null) {
            if (roleProvider.equals("LDAP")) {
                return new LdapUserDetailsService(userSearch(ldapProviderConfig), ldapAuthoritiesPopulator(ldapProviderConfig));
            } else if (roleProvider.equals("Database")) {
                return new LdapUserDetailsService(userSearch(ldapProviderConfig), ldapDatabaseAuthoritiesPopulator(
                    defaultAuthoritiesProvider(applicationContext, ldapProviderConfig)));
            }
        }
        return new LdapUserDetailsService(userSearch(ldapProviderConfig), ldapNullAuthoritiesPopulator());
    }

    @Bean(name = "LdapProviderConfig")
    public LdapProviderConfig ldapProviderConfig() {
        return new LdapProviderConfig();
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler securityContextLogoutHandler,
                                     LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter;
        if (rememberMeServices != null) {
            logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler, rememberMeServices);
        } else {
            logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler);
        }
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
