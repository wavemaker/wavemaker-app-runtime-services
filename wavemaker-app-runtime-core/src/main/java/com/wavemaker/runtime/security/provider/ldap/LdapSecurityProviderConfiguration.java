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
import org.springframework.core.env.Environment;
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

import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional({SecurityEnabledCondition.class, LdapProviderCondition.class})
public class LdapSecurityProviderConfiguration {

    @Autowired
    private Environment environment;

    @Bean(name = "contextSource")
    public ContextSource contextSource() {
        AbstractContextSource abstractContextSource = new WMSpringSecurityContextSource(environment.getProperty("security.providers.ldap.url"));
        String managerDn = environment.getProperty("security.providers.ldap.managerUsername");
        if (StringUtils.isNotBlank(managerDn)) {
            abstractContextSource.setUserDn(managerDn);
            abstractContextSource.setPassword(environment.getProperty("security.providers.ldap.managerPassword"));
        }
        return abstractContextSource;
    }

    @Bean(name = "defaultAuthoritiesProvider")
    @Conditional(LdapDatabaseAuthoritiesProviderCondition.class)
    public AuthoritiesProvider defaultAuthoritiesProvider(ApplicationContext applicationContext) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Boolean.parseBoolean(environment.getProperty("security.providers.ldap.isHQL")));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty("security.providers.ldap.rolesByUsernameQuery"));
        String modelName = environment.getProperty("security.providers.ldap.modelName");
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
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator =
            new DefaultLdapAuthoritiesPopulator(contextSource(), environment.getProperty("security.providers.ldap.groupSearchBase"));
        ldapAuthoritiesPopulator.setGroupSearchFilter(environment.getProperty("security.providers.ldap.groupSearchFilter"));
        ldapAuthoritiesPopulator.setGroupRoleAttribute(environment.getProperty("security.providers.ldap.groupRoleAttribute"));
        return ldapAuthoritiesPopulator;
    }

    @Bean(name = "userSearch")
    public LdapUserSearch userSearch() {
        return new FilterBasedLdapUserSearch(Objects.requireNonNull(environment.getProperty("security.providers.ldap.groupSearchBase")),
            Objects.requireNonNull(environment.getProperty("security.providers.ldap.userSearchPattern")),
            (BaseLdapPathContextSource) contextSource());
    }

    @Bean(name = "bindAuthenticator")
    public LdapAuthenticator bindAuthenticator() {
        BindAuthenticator bindAuthenticator = new BindAuthenticator((BaseLdapPathContextSource) contextSource());
        bindAuthenticator.setUserSearch(userSearch());
        String[] userDnPatterns = new String[]{environment.getProperty("security.providers.ldap.userSearchPattern")};
        bindAuthenticator.setUserDnPatterns(userDnPatterns);
        return bindAuthenticator;
    }

    @Bean(name = "ldapAuthenticationProvider")
    public AuthenticationProvider ldapAuthenticationProvider(ApplicationContext applicationContext) {
        LdapAuthenticationProvider ldapAuthenticationProvider;
        boolean groupSearchDisabled = Boolean.TRUE.equals(environment.getProperty("security.providers.ldap.groupSearchDisabled", Boolean.class));
        String roleProvider = environment.getProperty("security.providers.ldap.roleProvider");
        if (!groupSearchDisabled && roleProvider != null) {
            if (roleProvider.equals("LDAP")) {
                ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator(), ldapAuthoritiesPopulator());
                ldapAuthenticationProvider.setAuthoritiesMapper(authoritiesMapper());
                return ldapAuthenticationProvider;
            } else if (roleProvider.equals("Database")) {
                ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator(), ldapDatabaseAuthoritiesPopulator(
                    defaultAuthoritiesProvider(applicationContext)));
                ldapAuthenticationProvider.setAuthoritiesMapper(authoritiesMapper());
                return ldapAuthenticationProvider;
            }
        }
        ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator(), ldapNullAuthoritiesPopulator());
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
    public UserDetailsService ldapUserDetailsService(ApplicationContext applicationContext) {
        boolean groupSearchDisabled = Boolean.TRUE.equals(environment.getProperty("security.providers.ldap.groupSearchDisabled", Boolean.class));
        String roleProvider = environment.getProperty("security.providers.ldap.roleProvider");
        if (!groupSearchDisabled && roleProvider != null) {
            if (roleProvider.equals("LDAP")) {
                return new LdapUserDetailsService(userSearch(), ldapAuthoritiesPopulator());
            } else if (roleProvider.equals("Database")) {
                return new LdapUserDetailsService(userSearch(), ldapDatabaseAuthoritiesPopulator(
                    defaultAuthoritiesProvider(applicationContext)));
            }
        }
        return new LdapUserDetailsService(userSearch(), ldapNullAuthoritiesPopulator());
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler securityContextLogoutHandler,
                                     LogoutHandler wmCsrfLogoutHandler, PersistentTokenBasedRememberMeServices rememberMeServices) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler, rememberMeServices);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
