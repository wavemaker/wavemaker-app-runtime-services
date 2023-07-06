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

import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.runtime.security.cas.WMCasHttpsURLConnectionFactory;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledBaseConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.handler.WMCasAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional({SecurityEnabledCondition.class, CASSecurityProviderCondition.class})
public class CASSecurityProviderConfiguration implements WMSecurityConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name = "logoutSuccessHandler")
    public SimpleUrlLogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl(environment.getProperty("security.providers.cas.logoutUrl"));
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean(name = "redirectStrategyBean")
    public CASRedirectStrategy redirectStrategyBean() {
        return new CASRedirectStrategy();
    }

    @Bean(name = "casAuthenticationProvider")
    private CasAuthenticationProvider casAuthenticationProvider(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier) {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setServiceProperties(casServiceProperties());
        casAuthenticationProvider.setKey("casAuthProviderKey");
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator(appSSLSocketFactory, appHostnameVerifier));
        casAuthenticationProvider.setAuthenticationUserDetailsService(wmCasUserDetailsByNameServiceWrapper());
        return casAuthenticationProvider;
    }

    @Bean(name = "cas20ServiceTicketValidator")
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator(SSLSocketFactory appSSLSocketFactory, HostnameVerifier appHostnameVerifier) {
        Cas20ServiceTicketValidator cas20ServiceTicketValidator = new Cas20ServiceTicketValidator(Objects.requireNonNull(
            environment.getProperty("security.providers.cas.serverUrl")));
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
    public ServiceProperties casServiceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService("/");
        serviceProperties.setServiceParameter(environment.getProperty("security.providers.cas.serviceParameter"));
        serviceProperties.setArtifactParameter(environment.getProperty("security.providers.cas.artifactParameter"));
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    @Bean(name = "WMWebAuthenticationDetailsSource")
    public ServiceAuthenticationDetailsSource WMWebAuthenticationDetailsSource() {
        return new ServiceAuthenticationDetailsSource(casServiceProperties());

    }

    @Bean
    public CasAuthenticationFilter casFilter() {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setAuthenticationSuccessHandler(securityEnabledBaseConfiguration.successHandler());
        casAuthenticationFilter.setAuthenticationFailureHandler(securityEnabledBaseConfiguration.failureHandler());
        casAuthenticationFilter.setAuthenticationManager(securityEnabledBaseConfiguration.authenticationManager());
        casAuthenticationFilter.setAuthenticationDetailsSource(WMWebAuthenticationDetailsSource());
        casAuthenticationFilter.setServiceProperties(casServiceProperties());
        casAuthenticationFilter.setSessionAuthenticationStrategy(securityEnabledBaseConfiguration.compositeSessionAuthenticationStrategy());
        return casAuthenticationFilter;
    }

    @Bean(name = "WMSecAuthEntryPoint")
    public WMCASAuthenticationEntryPoint WMSecAuthEntryPoint() {
        WMCASAuthenticationEntryPoint authenticationEntryPoint = new WMCASAuthenticationEntryPoint();
        authenticationEntryPoint.setServiceProperties(casServiceProperties());
        authenticationEntryPoint.setLoginUrl(environment.getProperty("security.providers.cas.loginUrl"));
        return authenticationEntryPoint;
    }

    @Bean(name = "casAuthenticationSuccessHandler")
    public WMCasAuthenticationSuccessHandler casAuthenticationSuccessHandler() {
        return new WMCasAuthenticationSuccessHandler();
    }

    @Bean(name = " wmCasUserDetailsByNameServiceWrapper")
    public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> wmCasUserDetailsByNameServiceWrapper() {
        CASUserDetailsByNameServiceWrapper casUserDetailsByNameServiceWrapper = null;
        boolean isRoleMappingEnabled = Boolean.TRUE.equals(environment.getProperty("security.providers.cas.roleMappingEnabled", Boolean.class));
        String roleProvider = environment.getProperty("security.providers.cas.roleProvider");
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
    @Conditional(CASRoleProviderCondition.class)
    public DefaultAuthoritiesProviderImpl authoritiesProvider() {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Boolean.parseBoolean(environment.getProperty("security.providers.cas.isHql")));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty("security.providers.cas.rolesByUsernameQuery"));
        String modelName = environment.getProperty("security.providers.cas.modelName");
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(modelName + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(modelName + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "casUserDetailsService")
    public CASDatabaseUserDetailsService casUserDetailsService() {
        return new CASDatabaseUserDetailsService();
    }

    @Override
    public void addInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher("/j_spring_cas_security_check")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/services/security/ssologin")).authenticated();
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAt(casFilter(), CasAuthenticationFilter.class);
    }
}
