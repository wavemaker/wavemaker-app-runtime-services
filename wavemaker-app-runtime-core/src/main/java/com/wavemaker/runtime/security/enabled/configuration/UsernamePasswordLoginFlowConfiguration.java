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

package com.wavemaker.runtime.security.enabled.configuration;

import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.runtime.security.WMAuthenticationEntryPoint;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.entrypoint.AuthenticationEntryPointRegistry;
import com.wavemaker.runtime.security.filter.WMBasicAuthenticationFilter;
import com.wavemaker.runtime.security.handler.logout.LogoutSuccessHandlerRegistry;
import com.wavemaker.runtime.security.model.AuthProvider;
import com.wavemaker.runtime.security.model.AuthProviderType;
import com.wavemaker.runtime.security.model.AuthenticationMode;
import com.wavemaker.runtime.security.utils.SecurityPropertyUtils;

@Configuration
@Conditional({SecurityEnabledCondition.class, UsernamePasswordLoginFlowCondition.class})
public class UsernamePasswordLoginFlowConfiguration implements WMSecurityConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("successHandler")
    @Lazy
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    @Qualifier("failureHandler")
    @Lazy
    private AuthenticationFailureHandler failureHandler;

    @Autowired
    @Qualifier("compositeSessionAuthenticationStrategy")
    @Lazy
    private SessionAuthenticationStrategy compositeSessionAuthenticationStrategy;

    @Autowired(required = false)
    private RememberMeServices rememberMeServices;

    @Autowired
    @Lazy
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private LogoutSuccessHandlerRegistry logoutSuccessHandlerRegistry;

    @Autowired
    private AuthenticationEntryPointRegistry authenticationEntryPointRegistry;

    @PostConstruct
    public void init() {
        for (AuthProviderType authProviderType : AuthProviderType.values()) {
            if (authProviderType.getAuthenticationMode() == AuthenticationMode.USERNAME_PASSWORD) {
                logoutSuccessHandlerRegistry.registerLogoutSuccessHandler(authProviderType, logoutSuccessHandler());
                Set<AuthProvider> authProviders = SecurityPropertyUtils.getAuthProviderForType(environment, authProviderType);
                for (AuthProvider authProvider : authProviders) {
                    authenticationEntryPointRegistry.registerAuthenticationEntryPoint(authProvider, wmSecAuthEntryPoint());
                }
            }
        }
    }

    @Bean(name = "usernamePasswordFlowRedirectStrategy")
    public RedirectStrategy redirectStrategy() {
        return new DefaultRedirectStrategy();
    }

    @Bean(name = "usernamePasswordFlowLogoutSuccessHandler")
    public LogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategy());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean(name = "WMSecAuthEntryPoint")
    public AuthenticationEntryPoint wmSecAuthEntryPoint() {
        return new WMAuthenticationEntryPoint("/index.html");
    }

    @Bean(name = "WMBasicAuthenticationFilter")
    public Filter wmBasicAuthenticationFilter() {
        WMBasicAuthenticationFilter basicAuthenticationFilter = new WMBasicAuthenticationFilter(authenticationManager);
        basicAuthenticationFilter.setSecurityContextRepository(new NullSecurityContextRepository());
        return basicAuthenticationFilter;
    }

    @Bean(name = "WMSecAuthFilter")
    public Filter wmSecAuthFilter() {
        UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter();
        usernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        usernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);
        usernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager);
        usernamePasswordAuthenticationFilter.setFilterProcessesUrl("/j_spring_security_check");
        usernamePasswordAuthenticationFilter.setUsernameParameter("j_username");
        usernamePasswordAuthenticationFilter.setPasswordParameter("j_password");
        usernamePasswordAuthenticationFilter.setSessionAuthenticationStrategy(compositeSessionAuthenticationStrategy);
        if (rememberMeServices != null) {
            usernamePasswordAuthenticationFilter.setRememberMeServices(rememberMeServices);
        }
        usernamePasswordAuthenticationFilter.setSecurityContextRepository(securityContextRepository);
        return usernamePasswordAuthenticationFilter;
    }

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return List.of(new SecurityInterceptUrlEntry("/j_spring_security_check", Permission.PermitAll));
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAt(wmSecAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void addStatelessFilters(HttpSecurity http) {
        http.addFilterAt(wmBasicAuthenticationFilter(), BasicAuthenticationFilter.class);
    }
}
