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
import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.config.cas.CASProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.provider.cas.handler.WMCasAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.roles.RuntimeDatabaseRoleMappingConfig;

@Configuration
@Conditional({SecurityEnabledCondition.class, CASSecurityProviderCondition.class})
public class CASSecurityProviderConfiguration implements WMSecurityConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("successHandler")
    @Lazy
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    @Qualifier("failureHandler")
    @Lazy
    private AuthenticationFailureHandler failureHandler;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("compositeSessionAuthenticationStrategy")
    @Lazy
    private SessionAuthenticationStrategy compositeSessionAuthenticationStrategy;

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return List.of(new SecurityInterceptUrlEntry("/j_spring_cas_security_check", Permission.PermitAll));
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
    public LogoutSuccessHandler logoutSuccessHandler(CASProviderConfig casProviderConfig) {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl(casProviderConfig.getLogoutUrl());
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean(name = "redirectStrategyBean")
    public RedirectStrategy redirectStrategyBean() {
        return new CASRedirectStrategy();
    }

    @Bean(name = "casAuthenticationProvider")
    public AuthenticationProvider casAuthenticationProvider(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier,
                                                            CASProviderConfig casProviderConfig) {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setServiceProperties(casServiceProperties(casProviderConfig));
        casAuthenticationProvider.setKey("casAuthProviderKey");
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator(appSSLSocketFactory, appHostnameVerifier, casProviderConfig));
        casAuthenticationProvider.setAuthenticationUserDetailsService(wmCasUserDetailsByNameServiceWrapper(casProviderConfig));
        return casAuthenticationProvider;
    }

    @Bean(name = "cas20ServiceTicketValidator")
    public TicketValidator cas20ServiceTicketValidator(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier,
                                                       CASProviderConfig casProviderConfig) {
        Cas20ServiceTicketValidator cas20ServiceTicketValidator = new Cas20ServiceTicketValidator(Objects.requireNonNull(
            casProviderConfig.getServerUrl()));
        cas20ServiceTicketValidator.setURLConnectionFactory(casUrlConnectionFactory(appSSLSocketFactory, appHostnameVerifier));
        return cas20ServiceTicketValidator;
    }

    @Bean(name = "casUrlConnectionFactory")
    public HttpURLConnectionFactory casUrlConnectionFactory(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier) {
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
    public AuthenticationDetailsSource<HttpServletRequest, ?> wmWebAuthenticationDetailsSource(CASProviderConfig casProviderConfig) {
        return new ServiceAuthenticationDetailsSource(casServiceProperties(casProviderConfig));
    }

    @Bean(name = "casFilter")
    public Filter casFilter() {
        CASProviderConfig casProviderConfig = casProviderConfig();
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        casAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);
        casAuthenticationFilter.setAuthenticationManager(authenticationManager);
        casAuthenticationFilter.setAuthenticationDetailsSource(wmWebAuthenticationDetailsSource(casProviderConfig));
        casAuthenticationFilter.setServiceProperties(casServiceProperties(casProviderConfig));
        casAuthenticationFilter.setSessionAuthenticationStrategy(compositeSessionAuthenticationStrategy);
        return casAuthenticationFilter;
    }

    @Bean(name = "WMSecAuthEntryPoint")
    public AuthenticationEntryPoint wmSecAuthEntryPoint(CASProviderConfig casProviderConfig) {
        WMCASAuthenticationEntryPoint authenticationEntryPoint = new WMCASAuthenticationEntryPoint();
        authenticationEntryPoint.setServiceProperties(casServiceProperties(casProviderConfig));
        authenticationEntryPoint.setLoginUrl(casProviderConfig.getLoginUrl());
        return authenticationEntryPoint;
    }

    @Bean(name = "casAuthenticationSuccessHandler")
    public WMAuthenticationSuccessHandler casAuthenticationSuccessHandler() {
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
                casUserDetailsByNameServiceWrapper.setRoleAttributeName(environment.getProperty("security.providers.cas.roleAttributeName"));
                return casUserDetailsByNameServiceWrapper;
            } else if (roleProvider != null && roleProvider.equals("Database")) {
                CASDatabaseUserDetailsService casDatabaseUserDetailsService = new CASDatabaseUserDetailsService();
                casDatabaseUserDetailsService.setAuthoritiesProvider(authoritiesProvider());
                return new CASUserDetailsByNameServiceWrapper(casDatabaseUserDetailsService);
            }
        }
        return new CASUserDetailsByNameServiceWrapper(new CASUserDetailsService());
    }

    @Bean(name = "casAuthoritiesProvider")
    @Conditional(CASDatabaseRoleProviderCondition.class)
    public AuthoritiesProvider authoritiesProvider() {
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
    public UserDetailsService casUserDetailsService() {
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
