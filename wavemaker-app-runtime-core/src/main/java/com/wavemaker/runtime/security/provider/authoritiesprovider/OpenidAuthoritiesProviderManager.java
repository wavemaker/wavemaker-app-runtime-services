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
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.config.openid.OpenIdProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.DatabaseRoleMappingConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleAttributeNameMappingConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.openid.IdentityProviderUserAuthoritiesProvider;
import com.wavemaker.runtime.security.provider.openid.OpenIdProviderConfigRegistry;

public class OpenidAuthoritiesProviderManager {

    private static final String SECURITY_PROVIDERS_OPEN_ID = "security.providers.openId.";

    private Map<String, AuthoritiesProvider> authoritiesProviders = new ConcurrentHashMap<>();

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OpenIdProviderConfigRegistry openIdProviderConfigRegistry;

    public AuthoritiesProvider getAuthoritiesProvider(String providerId) {
        OpenIdProviderConfig openIdProviderConfig = openIdProviderConfigRegistry.getOpenIdProviderConfig(providerId);
        if (openIdProviderConfig.isRoleMappingEnabled()) {
            RoleMappingConfig roleMappingConfig = openIdProviderConfig.getRoleMappingConfig();
            return this.authoritiesProviders.computeIfAbsent(providerId, authoritiesProvider -> {
                if (roleMappingConfig instanceof RoleAttributeNameMappingConfig roleAttributeNameMappingConfig) {
                    return getOpenidAuthoritiesProvider(roleAttributeNameMappingConfig);
                } else if (roleMappingConfig instanceof DatabaseRoleMappingConfig databaseRoleMappingConfig) {
                    return getDatabaseAuthoritiesProvider(databaseRoleMappingConfig);
                }
                return null;
            });
        }
        return null;
    }

    private AuthoritiesProvider getOpenidAuthoritiesProvider(RoleAttributeNameMappingConfig roleAttributeNameMappingConfig) {
        IdentityProviderUserAuthoritiesProvider identityProviderUserAuthoritiesProvider = new IdentityProviderUserAuthoritiesProvider();
        identityProviderUserAuthoritiesProvider.setRoleAttributeName(roleAttributeNameMappingConfig.getRoleAttributeName());
        return identityProviderUserAuthoritiesProvider;
    }

    private AuthoritiesProvider getDatabaseAuthoritiesProvider(DatabaseRoleMappingConfig databaseRoleMappingConfig) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();

        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations)
            applicationContext.getBean(databaseRoleMappingConfig.getModelName() + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager)
            applicationContext.getBean(databaseRoleMappingConfig.getModelName() + "TransactionManager"));
        defaultAuthoritiesProvider.setHql(databaseRoleMappingConfig.getQueryType() == RoleQueryType.HQL);
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(databaseRoleMappingConfig.getRoleQuery());
        return defaultAuthoritiesProvider;
    }
}
