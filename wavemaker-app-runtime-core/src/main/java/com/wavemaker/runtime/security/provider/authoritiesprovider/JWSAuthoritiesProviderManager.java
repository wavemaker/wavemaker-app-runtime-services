/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.security.provider.authoritiesprovider;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

public class JWSAuthoritiesProviderManager {

    private static final String SECURITY_PROVIDERS_JWS = "security.providers.jws.";

    private Map<String, AuthoritiesProvider> authoritiesProviders = new ConcurrentHashMap<>();

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    public AuthoritiesProvider getAuthoritiesProvider(String providerId) {
        return this.authoritiesProviders.computeIfAbsent(providerId, authoritiesProvider -> {
            return getDatabaseAuthoritiesProvider(providerId);
        });
    }

    private AuthoritiesProvider getDatabaseAuthoritiesProvider(String providerId) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Objects.equals(environment.getProperty(SECURITY_PROVIDERS_JWS + providerId + ".queryType"), RoleQueryType.HQL));
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty(SECURITY_PROVIDERS_JWS + providerId + ".rolesByUsernameQuery"));
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext
            .getBean(environment.getProperty(SECURITY_PROVIDERS_JWS + providerId + ".modelName") + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager) applicationContext
            .getBean(environment.getProperty(SECURITY_PROVIDERS_JWS + providerId + ".modelName") + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }
}
