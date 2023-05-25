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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.csrf.WMCsrfLogoutHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional(LdapProviderCondition.class)
public class LdapConfiguration {
    @Value("${security.providers.ldap.groupSearchDisabled}")
    private boolean groupSearchDisabled;

    @Value("${security.providers.ldap.groupSearchBase}")
    private String groupSearchBase;
    @Value("${security.providers.ldap.roleProvider}")
    private String roleProvider;

    @Value("${security.providers.ldap.groupSearchFilter}")
    private String groupSearchFilter;
    @Value("${security.providers.ldap.groupRoleAttribute}")
    private String groupRoleAttribute;

    @Value("${security.providers.ldap.managerPassword}")
    private String managerPassword;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Bean(name = "contextSource")
    public ContextSource getContextSource() {
        AbstractContextSource abstractContextSource = new WMSpringSecurityContextSource(environment.getProperty("security.providers.ldap.url"));
        String managerDn = environment.getProperty("security.providers.ldap.managerUsername");
        if (StringUtils.isNotBlank(managerDn)) {
            abstractContextSource.setUserDn(managerDn);
            abstractContextSource.setPassword(managerPassword);
        }
        return abstractContextSource;
    }

    @Bean(name = "authoritiesProvider")
    @Conditional(LdapDatabaseAuthProviderCondition.class)
    public AuthoritiesProvider getAuthoritiesProvider() {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Boolean.parseBoolean(environment.getProperty("security.providers.ldap.isHql")));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty("security.providers.ldap.rolesByUsernameQuery"));
        String modelName = environment.getProperty("security.providers.ldap.modelName");
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(modelName + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(modelName + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "ldapDatabaseAuthoritiesPopulator")
    @Conditional(LdapDatabaseAuthProviderCondition.class)
    public LdapAuthoritiesPopulator getLdapDatabaseAuthoritiesPopulator(AuthoritiesProvider authoritiesProvider) {
        return new WMAuthoritiesPopulator(authoritiesProvider);
    }

    @Bean(name = "ldapNullAuthoritiesPopulator")
    @Conditional(LdapNullAuthProviderCondition.class)
    public LdapAuthoritiesPopulator getLdapNullAuthoritiesPopulator() {
        return new NullLdapAuthoritiesPopulator();
    }

    @Bean(name = "ldapAuthoritiesPopulator")
    @Conditional(LdapAuthProviderCondition.class)
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator =
            new DefaultLdapAuthoritiesPopulator(getContextSource(), groupSearchBase);
        ldapAuthoritiesPopulator.setGroupSearchFilter(groupSearchFilter);
        ldapAuthoritiesPopulator.setGroupRoleAttribute(groupRoleAttribute);
        return ldapAuthoritiesPopulator;
    }

    @Bean(name = "userSearch")
    public FilterBasedLdapUserSearch getFilterBasedLdapUserSearch() {
        return new FilterBasedLdapUserSearch(environment.getProperty("security.providers.ldap.groupSearchBase"),
            environment.getProperty("security.providers.ldap.userSearchPattern"),
            (BaseLdapPathContextSource) getContextSource());
    }

    @Bean(name = "bindAuthenticator")
    public BindAuthenticator getBindAuthenticator() {
        BindAuthenticator bindAuthenticator = new BindAuthenticator((BaseLdapPathContextSource) getContextSource());
        bindAuthenticator.setUserSearch(getFilterBasedLdapUserSearch());
        String[] userDnPatterns = new String[]{environment.getProperty("security.providers.ldap.userSearchPattern")};
        bindAuthenticator.setUserDnPatterns(userDnPatterns);
        return bindAuthenticator;
    }

    @Bean(name = "ldapAuthenticationProvider")
    public LdapAuthenticationProvider getLdapAuthenticationProvider() {
        if (!groupSearchDisabled) {
            if (roleProvider.equals("LDAP")) {
                return new LdapAuthenticationProvider(getBindAuthenticator(), ldapAuthoritiesPopulator());
            } else if (roleProvider.equals("Database")) {
                return new LdapAuthenticationProvider(getBindAuthenticator(), getLdapDatabaseAuthoritiesPopulator(getAuthoritiesProvider()));
            }
        }
        return new LdapAuthenticationProvider(getBindAuthenticator(), getLdapNullAuthoritiesPopulator());
    }

    @Bean(name = "ldapUserDetailsService")
    public LdapUserDetailsService getLdapUserDetailsService() {
        if (!groupSearchDisabled) {
            if (roleProvider.equals("LDAP")) {
                return new LdapUserDetailsService(getFilterBasedLdapUserSearch(), ldapAuthoritiesPopulator());
            } else if (roleProvider.equals("Database")) {
                return new LdapUserDetailsService(getFilterBasedLdapUserSearch(), getLdapDatabaseAuthoritiesPopulator(getAuthoritiesProvider()));
            }
        }
        return new LdapUserDetailsService(getFilterBasedLdapUserSearch(), getLdapNullAuthoritiesPopulator());
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(SimpleUrlLogoutSuccessHandler logoutSuccessHandler, SecurityContextLogoutHandler securityContextLogoutHandler,
                                     WMCsrfLogoutHandler wmCsrfLogoutHandler, PersistentTokenBasedRememberMeServices rememberMeServices) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler, rememberMeServices);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
