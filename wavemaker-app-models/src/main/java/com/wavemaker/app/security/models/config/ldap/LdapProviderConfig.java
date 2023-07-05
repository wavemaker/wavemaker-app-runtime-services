/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.ldap;

import com.wavemaker.app.security.models.annotation.NonProfilizableProperty;
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;

/**
 * @author Frankie Fu
 */
public class LdapProviderConfig extends AbstractProviderConfig {

    public static final String LDAP = "LDAP";

    @ProfilizableProperty("${security.providers.ldap.url}")
    private String url;

    @ProfilizableProperty("${security.providers.ldap.managerUsername:null}")
    private String managerDn;

    @ProfilizableProperty("${security.providers.ldap.managerPassword:null}")
    private String managerPassword;

    private String testDn;
    private String testPassword;

    @ProfilizableProperty("${security.providers.ldap.rootDn}")
    private String rootDn;

    @ProfilizableProperty("${security.providers.ldap.userSearchPattern}")
    private String userDnPattern;

    @ProfilizableProperty(value = "${security.providers.ldap.groupSearchDisabled}", isAutoUpdate = true)
    private boolean groupSearchDisabled;

    @ProfilizableProperty("${security.providers.ldap.groupSearchBase}")
    private String groupSearchBase;

    @ProfilizableProperty("${security.providers.ldap.groupRoleAttribute}")
    private String groupRoleAttribute;

    @ProfilizableProperty("${security.providers.ldap.groupSearchFilter}")
    private String groupSearchFilter;

    @NonProfilizableProperty("${security.providers.ldap.modelName:null}")
    private String roleModel;
    private String roleEntity;
    private String roleTable;
    private String roleUsername;
    private String roleProperty;

    private boolean useRolesQuery;

    @NonProfilizableProperty("${security.providers.ldap.rolesByUsernameQuery:null}")
    private String roleQuery;

    @ProfilizableProperty(value = "${security.providers.ldap.roleProvider}", isAutoUpdate = true)
    private String roleProvider;

    @NonProfilizableProperty("${security.providers.ldap.queryType:HQL}")
    private String queryType;

    @Override
    public String getType() {
        return LDAP;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getManagerDn() {
        return this.managerDn;
    }

    public void setManagerDn(String managerDn) {
        this.managerDn = managerDn;
    }

    public String getManagerPassword() {
        return this.managerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public String getTestDn() {
        return testDn;
    }

    public void setTestDn(final String testDn) {
        this.testDn = testDn;
    }

    public String getTestPassword() {
        return testPassword;
    }

    public void setTestPassword(final String testPassword) {
        this.testPassword = testPassword;
    }

    public String getRootDn() {
        return rootDn;
    }

    public void setRootDn(String rootDn) {
        this.rootDn = rootDn;
    }

    public String getUserDnPattern() {
        return this.userDnPattern;
    }

    public void setUserDnPattern(String userDnPattern) {
        this.userDnPattern = userDnPattern;
    }

    public boolean isGroupSearchDisabled() {
        return this.groupSearchDisabled;
    }

    public void setGroupSearchDisabled(boolean groupSearchDisabled) {
        this.groupSearchDisabled = groupSearchDisabled;
    }

    public String getGroupSearchBase() {
        return this.groupSearchBase;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getGroupRoleAttribute() {
        return this.groupRoleAttribute;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public String getGroupSearchFilter() {
        return this.groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getRoleModel() {
        return this.roleModel;
    }

    public void setRoleModel(String roleModel) {
        this.roleModel = roleModel;
    }

    public String getRoleEntity() {
        return this.roleEntity;
    }

    public void setRoleEntity(String roleEntity) {
        this.roleEntity = roleEntity;
    }

    public String getRoleTable() {
        return this.roleTable;
    }

    public void setRoleTable(String roleTable) {
        this.roleTable = roleTable;
    }

    public String getRoleUsername() {
        return this.roleUsername;
    }

    public void setRoleUsername(String roleUsername) {
        this.roleUsername = roleUsername;
    }

    public String getRoleProperty() {
        return this.roleProperty;
    }

    public void setRoleProperty(String roleProperty) {
        this.roleProperty = roleProperty;
    }

    public String getRoleQuery() {
        return this.roleQuery;
    }

    public void setRoleQuery(String roleQuery) {
        this.roleQuery = roleQuery;
    }

    public String getRoleProvider() {
        return this.roleProvider;
    }

    public void setRoleProvider(String roleProvider) {
        this.roleProvider = roleProvider;
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
