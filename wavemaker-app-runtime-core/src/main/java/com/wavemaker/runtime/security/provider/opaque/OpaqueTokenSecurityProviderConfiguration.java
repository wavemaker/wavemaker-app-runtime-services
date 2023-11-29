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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.servlet.Filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.config.opaque.OpaqueTokenProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.model.FilterInfo;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.roles.RuntimeDatabaseRoleMappingConfig;

@Configuration
@Conditional({SecurityEnabledCondition.class, OpaqueTokenSecurityProviderCondition.class})
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
    @Conditional(OpaqueDatabaseRoleProviderCondition.class)
    public AuthoritiesProvider opaqueAuthoritiesProvider(ApplicationContext applicationContext,
                                                         RuntimeDatabaseRoleMappingConfig opaqueRuntimeDatabaseRoleMappingConfig) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Objects.equals(opaqueRuntimeDatabaseRoleMappingConfig.getQueryType(), RoleQueryType.HQL));
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(opaqueRuntimeDatabaseRoleMappingConfig.getRolesByUsernameQuery());
        String modelName = opaqueRuntimeDatabaseRoleMappingConfig.getModelName();
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(modelName + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext.getBean(modelName + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "opaqueRuntimeDatabaseRoleMappingConfig")
    @Conditional(OpaqueDatabaseRoleProviderCondition.class)
    @ConfigurationProperties("security.providers.opaqueToken.database")
    public RuntimeDatabaseRoleMappingConfig runtimeDatabaseRoleMappingConfig() {
        return new RuntimeDatabaseRoleMappingConfig();
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
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return Collections.emptyList();
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAfter(opaqueBearerTokenAuthenticationFilter(), BasicAuthenticationFilter.class);
    }

    @Override
    public List<FilterInfo> getFilters() {
        return null;
    }
}
