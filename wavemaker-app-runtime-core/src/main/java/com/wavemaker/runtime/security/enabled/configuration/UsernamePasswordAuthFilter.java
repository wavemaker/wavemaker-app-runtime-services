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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.wavemaker.runtime.security.WMAuthenticationEntryPoint;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.filter.WMBasicAuthenticationFilter;
import com.wavemaker.runtime.security.rememberme.config.RememberMeConfiguration;

@Configuration
@Conditional(UsernamePAsswordAuthFilterCondition.class)
public class UsernamePasswordAuthFilter implements WMSecurityConfiguration {

    @Autowired(required = false)
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;
    @Autowired(required = false)
    private RememberMeConfiguration rememberMeConfiguration;

    @Value("${security.providers.activeProviders}")
    private String activeProvider;

    @Bean
    public DefaultRedirectStrategy redirectStrategyBean() {
        return new DefaultRedirectStrategy();
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean
    public WMAuthenticationEntryPoint wmSecAuthEntryPoint() {
        return new WMAuthenticationEntryPoint("/index.html");
    }

    @Bean
    public WMBasicAuthenticationFilter wmBasicAuthenticationFilter(HttpSecurity http) {
        return new WMBasicAuthenticationFilter(securityEnabledBaseConfiguration.authenticationManager(http));
    }

    @Bean(name = "WMSecAuthFilter")
    public UsernamePasswordAuthenticationFilter wmSecAuthFilter(HttpSecurity http) {
        UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter();
        usernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(securityEnabledBaseConfiguration.successHandler());
        usernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(securityEnabledBaseConfiguration.failureHandler());
        usernamePasswordAuthenticationFilter.setAuthenticationManager(securityEnabledBaseConfiguration.authenticationManager(http));
        usernamePasswordAuthenticationFilter.setFilterProcessesUrl("/j_spring_security_check");
        usernamePasswordAuthenticationFilter.setUsernameParameter("j_username");
        usernamePasswordAuthenticationFilter.setPasswordParameter("j_password");
        usernamePasswordAuthenticationFilter.setSessionAuthenticationStrategy(securityEnabledBaseConfiguration.compositeSessionAuthenticationStrategy());
        if (activeProvider.equals("AD") || activeProvider.equals("CUSTOM")) {
            return usernamePasswordAuthenticationFilter;
        }
        usernamePasswordAuthenticationFilter.setRememberMeServices(rememberMeConfiguration.rememberMeServices());
        return usernamePasswordAuthenticationFilter;
    }

    @Override
    public void executeInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        authorizeRequestsCustomizer.antMatchers("/j_spring_security_check").permitAll();
    }

    @Override
    public void executeFilters(HttpSecurity http) {
        http.addFilterAt(wmBasicAuthenticationFilter(http), BasicAuthenticationFilter.class);
        http.addFilterAt(wmSecAuthFilter(http), UsernamePasswordAuthenticationFilter.class);
    }
}
