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

package com.wavemaker.runtime.security.provider.opaque;

import java.util.Objects;

import javax.servlet.Filter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.config.opaque.OpaqueTokenProviderConfig;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional({SecurityEnabledCondition.class, OpaqueProviderCondition.class})
public class OpaqueTokenSecurityProviderConfiguration implements WMSecurityConfiguration {

    @Bean(name = "nimbusOpaqueTokenIntrospector")
    public OpaqueTokenIntrospector nimbusOpaqueTokenIntrospector() {
        OpaqueTokenProviderConfig opaqueTokenProviderConfig = opaqueTokenProviderConfig();
        return new NimbusOpaqueTokenIntrospector(Objects.requireNonNull(opaqueTokenProviderConfig.getIntrospectionUrl()),
            Objects.requireNonNull(opaqueTokenProviderConfig.getClientId()),
            Objects.requireNonNull(opaqueTokenProviderConfig.getClientSecret()));
    }

    @Bean(name = "opaqueAuthenticationConverter")
    public OpaqueTokenAuthenticationConverter opaqueAuthenticationConverter() {
        return new OpaqueAuthenticationConverter();
    }

    @Bean(name = "opaqueTokenAuthenticationProvider")
    public AuthenticationProvider opaqueTokenAuthenticationProvider() {
        OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider = new OpaqueTokenAuthenticationProvider(nimbusOpaqueTokenIntrospector());
        opaqueTokenAuthenticationProvider.setAuthenticationConverter(opaqueAuthenticationConverter());
        return opaqueTokenAuthenticationProvider;
    }

    @Bean(name = "opaqueAuthoritiesProvider")
    @Conditional(OpaqueRoleProviderCondition.class)
    public AuthoritiesProvider opaqueAuthoritiesProvider(Environment environment, ApplicationContext applicationContext) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Boolean.parseBoolean(environment.getProperty("security.providers.opaqueToken.isHQL")));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty("security.providers.opaqueToken.rolesByUsernameQuery"));
        String modelName = environment.getProperty("security.providers.opaqueToken.modelName");
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(modelName + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(modelName + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "providerManager")
    public AuthenticationManager providerManager() {
        return new ProviderManager(opaqueTokenAuthenticationProvider());
    }

    @Bean(name = "opaqueBearerTokenAuthenticationFilter")
    public Filter opaqueBearerTokenAuthenticationFilter() {
        return new BearerTokenAuthenticationFilter(providerManager());
    }

    @Bean(name = "OpaqueTokenProviderConfig")
    public OpaqueTokenProviderConfig opaqueTokenProviderConfig() {
        return new OpaqueTokenProviderConfig();
    }

    @Override
    public void addInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        //No InterceptorUrls here
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAfter(opaqueBearerTokenAuthenticationFilter(), BasicAuthenticationFilter.class);
    }
}
