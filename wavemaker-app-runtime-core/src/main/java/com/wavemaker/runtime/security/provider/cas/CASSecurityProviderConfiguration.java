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

package com.wavemaker.runtime.security.provider.cas;

import java.util.List;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.Filter;

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.config.cas.CASProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledBaseConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.cas.handler.WMCasAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.roles.RuntimeDatabaseRoleMappingConfig;

@Configuration
@Conditional({SecurityEnabledCondition.class, CASSecurityProviderCondition.class})
public class CASSecurityProviderConfiguration implements WMSecurityConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return List.of(new SecurityInterceptUrlEntry("/j_spring_cas_security_check", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/services/security/ssologin", Permission.Authenticated));
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAt(casFilter(), CasAuthenticationFilter.class);
    }

    @Bean(name = "casProviderConfig")
    public CASProviderConfig casProviderConfig() {
        return new CASProviderConfig();
    }

    @Bean(name = "logoutSuccessHandler")
    public SimpleUrlLogoutSuccessHandler logoutSuccessHandler(CASProviderConfig casProviderConfig) {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl(casProviderConfig.getLogoutUrl());
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean(name = "redirectStrategyBean")
    public CASRedirectStrategy redirectStrategyBean() {
        return new CASRedirectStrategy();
    }

    @Bean(name = "casAuthenticationProvider")
    public CasAuthenticationProvider casAuthenticationProvider(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier,
                                                               CASProviderConfig casProviderConfig) {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setServiceProperties(casServiceProperties(casProviderConfig));
        casAuthenticationProvider.setKey("casAuthProviderKey");
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator(appSSLSocketFactory, appHostnameVerifier, casProviderConfig));
        casAuthenticationProvider.setAuthenticationUserDetailsService(wmCasUserDetailsByNameServiceWrapper(casProviderConfig));
        return casAuthenticationProvider;
    }

    @Bean(name = "cas20ServiceTicketValidator")
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier,
                                                                   CASProviderConfig casProviderConfig) {
        Cas20ServiceTicketValidator cas20ServiceTicketValidator = new Cas20ServiceTicketValidator(Objects.requireNonNull(
            casProviderConfig.getServerUrl()));
        cas20ServiceTicketValidator.setURLConnectionFactory(casUrlConnectionFactory(appSSLSocketFactory, appHostnameVerifier));
        return cas20ServiceTicketValidator;
    }

    @Bean(name = "casUrlConnectionFactory")
    public WMCasHttpsURLConnectionFactory casUrlConnectionFactory(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier) {
        WMCasHttpsURLConnectionFactory wmCasHttpsURLConnectionFactory = new WMCasHttpsURLConnectionFactory();
        wmCasHttpsURLConnectionFactory.setSslSocketFactory(appSSLSocketFactory);
        wmCasHttpsURLConnectionFactory.setHostnameVerifier(appHostnameVerifier);
        return wmCasHttpsURLConnectionFactory;
    }

    @Bean(name = "casServiceProperties")
    public ServiceProperties casServiceProperties(CASProviderConfig casProviderConfig) {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService("/");
        serviceProperties.setServiceParameter(casProviderConfig.getServiceParameter());
        serviceProperties.setArtifactParameter(casProviderConfig.getArtifactParameter());
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    @Bean(name = "WMWebAuthenticationDetailsSource")
    public ServiceAuthenticationDetailsSource wmWebAuthenticationDetailsSource(CASProviderConfig casProviderConfig) {
        return new ServiceAuthenticationDetailsSource(casServiceProperties(casProviderConfig));

    }

    @Bean(name = "casFilter")
    public Filter casFilter() {
        CASProviderConfig casProviderConfig = casProviderConfig();
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setAuthenticationSuccessHandler(securityEnabledBaseConfiguration.successHandler());
        casAuthenticationFilter.setAuthenticationFailureHandler(securityEnabledBaseConfiguration.failureHandler());
        casAuthenticationFilter.setAuthenticationManager(securityEnabledBaseConfiguration.authenticationManager());
        casAuthenticationFilter.setAuthenticationDetailsSource(wmWebAuthenticationDetailsSource(casProviderConfig));
        casAuthenticationFilter.setServiceProperties(casServiceProperties(casProviderConfig));
        casAuthenticationFilter.setSessionAuthenticationStrategy(securityEnabledBaseConfiguration.compositeSessionAuthenticationStrategy());
        return casAuthenticationFilter;
    }

    @Bean(name = "WMSecAuthEntryPoint")
    public WMCASAuthenticationEntryPoint wmSecAuthEntryPoint(CASProviderConfig casProviderConfig) {
        WMCASAuthenticationEntryPoint authenticationEntryPoint = new WMCASAuthenticationEntryPoint();
        authenticationEntryPoint.setServiceProperties(casServiceProperties(casProviderConfig));
        authenticationEntryPoint.setLoginUrl(casProviderConfig.getLoginUrl());
        return authenticationEntryPoint;
    }

    @Bean(name = "casAuthenticationSuccessHandler")
    public WMCasAuthenticationSuccessHandler casAuthenticationSuccessHandler() {
        return new WMCasAuthenticationSuccessHandler();
    }

    @Bean(name = " wmCasUserDetailsByNameServiceWrapper")
    public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> wmCasUserDetailsByNameServiceWrapper(CASProviderConfig casProviderConfig) {
        CASUserDetailsByNameServiceWrapper casUserDetailsByNameServiceWrapper = null;
        boolean isRoleMappingEnabled = casProviderConfig.isRoleMappingEnabled();
        String roleProvider = casProviderConfig.getRoleProvider();
        if (isRoleMappingEnabled) {
            if (roleProvider != null && roleProvider.equals("CAS")) {
                casUserDetailsByNameServiceWrapper = new CASUserDetailsByNameServiceWrapper(casUserDetailsService());
                casUserDetailsByNameServiceWrapper.setRoleAttributeName(environment.getProperty("security.providers.cas.roleAttribute"));
                return casUserDetailsByNameServiceWrapper;
            } else if (roleProvider != null && roleProvider.equals("Database")) {
                CASDatabaseUserDetailsService casDatabaseUserDetailsService = new CASDatabaseUserDetailsService();
                casDatabaseUserDetailsService.setAuthoritiesProvider(authoritiesProvider());
                return new CASUserDetailsByNameServiceWrapper(casDatabaseUserDetailsService);
            }
        }
        return new CASUserDetailsByNameServiceWrapper(new CASUserDetailsService());
    }

    @Bean
    @Conditional(CASDatabaseRoleProviderCondition.class)
    public DefaultAuthoritiesProviderImpl authoritiesProvider() {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        RuntimeDatabaseRoleMappingConfig runtimeDatabaseRoleMappingConfig = runtimeDatabaseRoleMappingConfig();
        defaultAuthoritiesProvider.setHql(runtimeDatabaseRoleMappingConfig.getQueryType() == RoleQueryType.HQL);
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(runtimeDatabaseRoleMappingConfig.getRolesByUsernameQuery());
        String modelName = runtimeDatabaseRoleMappingConfig.getModelName();
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(modelName + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(modelName + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "runtimeDatabaseRoleMappingConfig")
    @Conditional(CASDatabaseRoleProviderCondition.class)
    @ConfigurationProperties("security.providers.cas.database")
    public RuntimeDatabaseRoleMappingConfig runtimeDatabaseRoleMappingConfig() {
        return new RuntimeDatabaseRoleMappingConfig();
    }

    @Bean(name = "casUserDetailsService")
    public CASDatabaseUserDetailsService casUserDetailsService() {
        return new CASDatabaseUserDetailsService();
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler securityContextLogoutHandler,
                                     LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
