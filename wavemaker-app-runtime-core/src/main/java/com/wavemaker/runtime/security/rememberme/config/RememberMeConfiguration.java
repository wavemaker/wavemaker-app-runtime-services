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

package com.wavemaker.runtime.security.rememberme.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledBaseConfiguration;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMCsrfTokenRepositorySuccessHandler;
import com.wavemaker.runtime.security.handler.WMSecurityContextRepositorySuccessHandler;
import com.wavemaker.runtime.security.provider.database.DatabaseConfiguration;
import com.wavemaker.runtime.security.provider.demo.DemoConfiguration;
import com.wavemaker.runtime.security.provider.ldap.LdapConfiguration;
import com.wavemaker.runtime.security.rememberme.WMRememberMeAuthenticationFilter;

@Configuration
@Conditional(RememberMeConfigCondition.class)
public class RememberMeConfiguration implements WMSecurityConfiguration {

    @Autowired(required = false)
    SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;

    @Autowired(required = false)
    DatabaseConfiguration databaseConfiguration;

    @Autowired(required = false)
    LdapConfiguration ldapConfiguration;

    @Autowired(required = false)
    DemoConfiguration demoConfiguration;

    @Value("${security.general.rememberMe.timeOut}")
    int tokenValiditySeconds;

    @Value("${security.providers.activeProviders}")
    String activeProvider;

    @Bean
    public PersistentTokenBasedRememberMeServices rememberMeServices() {
        PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices = new PersistentTokenBasedRememberMeServices("WM_APP_KEY", getUserDetailsService(), rememberMeRepository());
        persistentTokenBasedRememberMeServices.setParameter("j_rememberme");
        persistentTokenBasedRememberMeServices.setTokenValiditySeconds(tokenValiditySeconds);
        return persistentTokenBasedRememberMeServices;
    }

    @Bean(name = "rememberMeAuthFilter")
    public WMRememberMeAuthenticationFilter rememberMeAuthFilter(HttpSecurity http) {
        WMRememberMeAuthenticationFilter wmRememberMeAuthenticationFilter = new WMRememberMeAuthenticationFilter(
            securityEnabledBaseConfiguration.authenticationManager(http), rememberMeServices());
        wmRememberMeAuthenticationFilter.setAuthenticationSuccessHandler(rememberMeAuthenticationSuccessHandler());
        return wmRememberMeAuthenticationFilter;
    }

    @Bean
    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider("WM_APP_KEY");
    }

    @Bean
    public InMemoryTokenRepositoryImpl rememberMeRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean
    public WMApplicationAuthenticationSuccessHandler rememberMeAuthenticationSuccessHandler() {
        WMApplicationAuthenticationSuccessHandler wmApplicationAuthenticationSuccessHandler = new WMApplicationAuthenticationSuccessHandler();
        List<AuthenticationSuccessHandler> defaultSuccessHandlerList = new ArrayList<>();
        defaultSuccessHandlerList.add(wmSecurityContextRepositorySuccessHandler());
        defaultSuccessHandlerList.add(wmCsrfTokenRepositorySuccessHandler());
        wmApplicationAuthenticationSuccessHandler.setDefaultSuccessHandlerList(defaultSuccessHandlerList);
        return wmApplicationAuthenticationSuccessHandler;
    }

    @Bean
    public WMSecurityContextRepositorySuccessHandler wmSecurityContextRepositorySuccessHandler() {
        return new WMSecurityContextRepositorySuccessHandler(securityEnabledBaseConfiguration.securityContextRepository());
    }

    @Bean
    public WMCsrfTokenRepositorySuccessHandler wmCsrfTokenRepositorySuccessHandler() {
        return new WMCsrfTokenRepositorySuccessHandler(securityEnabledBaseConfiguration.csrfTokenRepository());
    }

    public UserDetailsService getUserDetailsService() {
        switch (activeProvider) {
            case "DEMO":
                return demoConfiguration.demoUserDetailsService();
            case "DATABASE":
                return databaseConfiguration.getDatabaseUserDetailsService();
            case "LDAP":
                return ldapConfiguration.getLdapUserDetailsService();
            default:
                DemoConfiguration demoConfigurationForDefault = new DemoConfiguration();
                return demoConfigurationForDefault.demoUserDetailsService();
        }
    }

    @Override
    public void executeInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        //No interceptUrls in rememberme configuration
    }

    @Override
    public void executeFilters(HttpSecurity http) {
        http.addFilterAt(rememberMeAuthFilter(http), RememberMeAuthenticationFilter.class);
    }
}
