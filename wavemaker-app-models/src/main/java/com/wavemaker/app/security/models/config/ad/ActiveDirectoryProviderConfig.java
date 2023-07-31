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
package com.wavemaker.app.security.models.config.ad;

import com.wavemaker.app.security.models.annotation.NonProfilizableProperty;
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;

public class ActiveDirectoryProviderConfig extends AbstractProviderConfig {

    public static final String DIRECTORY = "AD";

    @ProfilizableProperty("${security.providers.ad.url}")
    private String url;

    @ProfilizableProperty("${security.providers.ad.domain}")
    private String domain;

    @ProfilizableProperty("${security.providers.ad.rootDn}")
    private String rootDn;

    @ProfilizableProperty("${security.providers.ad.userSearchPattern}")
    private String userSearchPattern;

    @NonProfilizableProperty("${security.providers.ad.testUsername}")
    private String testUsername;

    @NonProfilizableProperty("${security.providers.ad.testPassword}")
    private String testPassword;

    @ProfilizableProperty(value = "${security.providers.ad.groupSearchDisabled}", isAutoUpdate = true)
    private boolean groupSearchDisabled;

    @ProfilizableProperty(value = "${security.providers.ad.roleProvider}", isAutoUpdate = true)
    private String roleProvider;

    @ProfilizableProperty("${security.providers.ad.groupRoleAttribute}")
    private String groupRoleAttribute;

    @NonProfilizableProperty("${security.providers.ad.database.modelName}")
    private String roleModel;

    private String roleEntity;

    private String roleTable;

    private String roleUsername;

    private String roleProperty;

    private boolean useRolesQuery;

    @NonProfilizableProperty("${security.providers.ad.database.rolesByUsernameQuery}")
    private String roleQuery;

    @NonProfilizableProperty("${security.providers.ad.database.queryType}")
    private String queryType;

    @Override
    public String getType() {
        return DIRECTORY;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRootDn() {
        return rootDn;
    }

    public void setRootDn(final String rootDn) {
        this.rootDn = rootDn;
    }

    public String getUserSearchPattern() {
        return userSearchPattern;
    }

    public void setUserSearchPattern(final String userSearchPattern) {
        this.userSearchPattern = userSearchPattern;
    }

    public String getTestUsername() {
        return testUsername;
    }

    public void setTestUsername(final String testUsername) {
        this.testUsername = testUsername;
    }

    public String getTestPassword() {
        return testPassword;
    }

    public void setTestPassword(final String testPassword) {
        this.testPassword = testPassword;
    }

    public String getRoleModel() {
        return roleModel;
    }

    public void setRoleModel(String roleModel) {
        this.roleModel = roleModel;
    }

    public String getRoleEntity() {
        return roleEntity;
    }

    public void setRoleEntity(String roleEntity) {
        this.roleEntity = roleEntity;
    }

    public String getRoleTable() {
        return roleTable;
    }

    public void setRoleTable(String roleTable) {
        this.roleTable = roleTable;
    }

    public String getRoleUsername() {
        return roleUsername;
    }

    public void setRoleUsername(String roleUsername) {
        this.roleUsername = roleUsername;
    }

    public String getRoleProperty() {
        return roleProperty;
    }

    public void setRoleProperty(String roleProperty) {
        this.roleProperty = roleProperty;
    }

    public String getRoleQuery() {
        return roleQuery;
    }

    public void setRoleQuery(String roleQuery) {
        this.roleQuery = roleQuery;
    }

    public String getRoleProvider() {
        return roleProvider;
    }

    public void setRoleProvider(String roleProvider) {
        this.roleProvider = roleProvider;
    }

    public boolean isGroupSearchDisabled() {
        return groupSearchDisabled;
    }

    public void setGroupSearchDisabled(boolean groupSearchDisabled) {
        this.groupSearchDisabled = groupSearchDisabled;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public boolean isUseRolesQuery() {
        return useRolesQuery;
    }

    public void setUseRolesQuery(boolean useRolesQuery) {
        this.useRolesQuery = useRolesQuery;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(final String queryType) {
        this.queryType = queryType;
    }
}
