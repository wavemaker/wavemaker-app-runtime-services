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

package com.wavemaker.runtime.security.provider.jws;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.app.security.models.jws.JWSConfiguration;
import com.wavemaker.app.security.models.jws.JWSProviderConfiguration;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.authoritiesprovider.JWSAuthoritiesProviderManager;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional({SecurityEnabledCondition.class, JWSProviderCondition.class})
public class JWSSecurityProviderConfiguration implements WMSecurityConfiguration {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name = "jwsAuthenticationManagerResolver")
    public AuthenticationManagerResolver<String> jwsAuthenticationManagerResolver() {
        return new JWSAuthenticationManagerResolver();
    }

    @Bean(name = "jwtIssuerAuthenticationManagerResolver")
    public AuthenticationManagerResolver<HttpServletRequest> jwtIssuerAuthenticationManagerResolver() {
        return new JwtIssuerAuthenticationManagerResolver(jwsAuthenticationManagerResolver());
    }

    @Bean(name = "jwsBearerTokenAuthenticationFilter")
    public Filter jwsBearerTokenAuthenticationFilter() {
        BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter = new BearerTokenAuthenticationFilter(jwtIssuerAuthenticationManagerResolver());
        bearerTokenAuthenticationFilter.setSecurityContextRepository(new NullSecurityContextRepository());
        return bearerTokenAuthenticationFilter;
    }

    @Bean
    @ConfigurationProperties(prefix = "security.providers")
    public JWSConfiguration jwsConfiguration() {
        return new JWSConfiguration();
    }

    @Bean
    public JWSAuthoritiesProviderManager jwsAuthoritiesProviderManager() {
        return new JWSAuthoritiesProviderManager();
    }

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return Collections.emptyList();
    }

    @Override
    public void addStatelessFilters(HttpSecurity http) {
        http.addFilterAfter(jwsBearerTokenAuthenticationFilter(), BasicAuthenticationFilter.class);
    }

    private DefaultAuthoritiesProviderImpl jwsAuthoritiesProvider(JWSProviderConfiguration jwsProviderConfiguration) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Objects.equals(jwsProviderConfiguration.getQueryType(), RoleQueryType.HQL));
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(jwsProviderConfiguration.getRolesByUsernameQuery());
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext
            .getBean(jwsProviderConfiguration.getModelName() + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext
            .getBean(jwsProviderConfiguration.getModelName() + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }
}
