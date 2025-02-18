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
package com.wavemaker.runtime.security.provider.openid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.wavemaker.app.security.models.config.openid.OpenIdProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.DatabaseRoleMappingConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleAttributeNameMappingConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleQueryType;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.constants.SecurityConstants;
import com.wavemaker.runtime.security.model.AuthProvider;
import com.wavemaker.runtime.security.model.AuthProviderType;
import com.wavemaker.runtime.security.utils.SecurityPropertyUtils;

/**
 * Created by srujant on 30/7/18.
 */
public class OpenIdProviderConfigRegistry {

    @Autowired
    private Environment environment;

    private final Map<String, OpenIdProviderConfig> openIdProviderConfigMap = new ConcurrentHashMap<>();

    private static final String SECURITY_PROVIDERS_OPEN_ID = "security.providers.openId.";

    public OpenIdProviderConfig getOpenIdProviderConfig(String providerId) {
        return openIdProviderConfigMap.computeIfAbsent(providerId, this::constructOpenIdProviderConfig);
    }

    protected OpenIdProviderConfig constructOpenIdProviderConfig(String providerId) {
        validateProviderIdIsActive(providerId);
        OpenIdProviderConfig openIdProviderConfig = new OpenIdProviderConfig();
        openIdProviderConfig.setProviderId(providerId);
        openIdProviderConfig.setClientId(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".clientId"));
        openIdProviderConfig.setClientSecret(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".clientSecret"));
        openIdProviderConfig.setAuthorizationUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".authorizationUrl"));
        openIdProviderConfig.setJwkSetUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".jwkSetUrl"));
        openIdProviderConfig.setLogoutUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".logoutUrl"));
        openIdProviderConfig.setTokenUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".tokenUrl"));
        openIdProviderConfig.setUserInfoUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".userInfoUrl"));
        openIdProviderConfig.setRedirectUrlTemplate("{baseUrl}/oauth2/code/{registrationId}");
        openIdProviderConfig.setUserNameAttributeName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".userNameAttributeName"));
        String scopes = environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".scopes");
        List<String> scopesList = new ArrayList<>();
        if (scopes != null) {
            Collections.addAll(scopesList, scopes.split(","));
        }
        String openIdScope = OpenIdConstants.OPEN_ID_SCOPE;
        if (!scopesList.contains(openIdScope)) {
            scopesList.add(openIdScope);
        }
        openIdProviderConfig.setScopes(scopesList);
        boolean roleMappingEnabled = Boolean.TRUE.equals(environment.getProperty("security.providers.openId." + providerId + ".roleMappingEnabled", Boolean.class));
        if (roleMappingEnabled) {
            openIdProviderConfig.setRoleMappingEnabled(roleMappingEnabled);
            String roleProvider = environment.getProperty("security.providers.openId." + providerId + ".roleProvider");
            if (SecurityConstants.OPENID_PROVIDER.equals(roleProvider)) {
                RoleAttributeNameMappingConfig roleAttributeNameMappingConfig = new RoleAttributeNameMappingConfig();
                roleAttributeNameMappingConfig.setRoleAttributeName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".roleAttributeName"));
                openIdProviderConfig.setRoleMappingConfig(roleAttributeNameMappingConfig);
            } else if ("Database".equals(roleProvider)) {
                DatabaseRoleMappingConfig databaseRoleMappingConfig = new DatabaseRoleMappingConfig();
                databaseRoleMappingConfig.setModelName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".database.modelName"));
                databaseRoleMappingConfig.setQueryType(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".database.queryType", RoleQueryType.class));
                databaseRoleMappingConfig.setRoleQuery(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".database.rolesByUsernameQuery"));
                openIdProviderConfig.setRoleMappingConfig(databaseRoleMappingConfig);
            }
        }
        return openIdProviderConfig;
    }

    protected void validateProviderIdIsActive(String providerId) {
        Set<AuthProvider> openIdActiveProviders = SecurityPropertyUtils.getAuthProviderForType(environment, AuthProviderType.OPENID);
        if (openIdActiveProviders.stream().map(AuthProvider::getProviderId).noneMatch(Predicate.isEqual(providerId))) {
            throw new WMRuntimeException("No open id provider found with id:" + providerId);
        }
    }
}
