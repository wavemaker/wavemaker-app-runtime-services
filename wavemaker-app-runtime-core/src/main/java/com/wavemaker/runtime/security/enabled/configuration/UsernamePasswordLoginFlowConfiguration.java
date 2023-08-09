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

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.runtime.security.WMAuthenticationEntryPoint;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.filter.WMBasicAuthenticationFilter;

@Configuration
@Conditional({SecurityEnabledCondition.class, UsernamePasswordLoginFlowCondition.class})
public class UsernamePasswordLoginFlowConfiguration implements WMSecurityConfiguration {

    @Autowired
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;

    @Autowired(required = false)
    private RememberMeServices rememberMeServices;

    @Bean(name = "redirectStrategyBean")
    public RedirectStrategy redirectStrategyBean() {
        return new DefaultRedirectStrategy();
    }

    @Bean(name = "logoutSuccessHandler")
    public LogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean(name = "WMSecAuthEntryPoint")
    public AuthenticationEntryPoint WMSecAuthEntryPoint() {
        return new WMAuthenticationEntryPoint("/index.html");
    }

    @Bean(name = "WMBasicAuthenticationFilter")
    public Filter wmBasicAuthenticationFilter() {
        return new WMBasicAuthenticationFilter(securityEnabledBaseConfiguration.authenticationManager());
    }

    @Bean(name = "WMSecAuthFilter")
    public Filter wmSecAuthFilter() {
        UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter();
        usernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(securityEnabledBaseConfiguration.successHandler());
        usernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(securityEnabledBaseConfiguration.failureHandler());
        usernamePasswordAuthenticationFilter.setAuthenticationManager(securityEnabledBaseConfiguration.authenticationManager());
        usernamePasswordAuthenticationFilter.setFilterProcessesUrl("/j_spring_security_check");
        usernamePasswordAuthenticationFilter.setUsernameParameter("j_username");
        usernamePasswordAuthenticationFilter.setPasswordParameter("j_password");
        usernamePasswordAuthenticationFilter.setSessionAuthenticationStrategy(securityEnabledBaseConfiguration.compositeSessionAuthenticationStrategy());
        if (rememberMeServices != null) {
            usernamePasswordAuthenticationFilter.setRememberMeServices(rememberMeServices);
        }
        return usernamePasswordAuthenticationFilter;
    }

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return List.of(new SecurityInterceptUrlEntry("/j_spring_security_check", Permission.PermitAll));
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAt(wmBasicAuthenticationFilter(), BasicAuthenticationFilter.class);
        http.addFilterAt(wmSecAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
