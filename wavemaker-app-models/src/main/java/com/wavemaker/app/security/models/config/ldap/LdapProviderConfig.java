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
package com.wavemaker.app.security.models.config.ldap;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.wavemaker.app.security.models.annotation.NonProfilizableProperty;
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Frankie Fu
 */
@Schema(title = "LDAP Security provider")
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

    @NonProfilizableProperty("${security.providers.ldap.rootDn}")
    private String rootDn;

    @ProfilizableProperty("${security.providers.ldap.userSearchPattern}")
    private String userDnPattern;

    @ProfilizableProperty(value = "${security.providers.ldap.roleMappingEnabled}", autoUpdate = true)
    private boolean roleMappingEnabled;

    @ProfilizableProperty("${security.providers.ldap.groupSearchBase}")
    private String groupSearchBase;

    @ProfilizableProperty(value = "${security.providers.ldap.roleProvider}", autoUpdate = true)
    @JsonPropertyDescription("Role provider can be 'Database' or 'LDAP'")
    private String roleProvider;

    @JsonPropertyDescription("For roleMappingConfig, there can only be two allowed types DatabaseRoleMappingConfig, LdapRoleMappingConfig")
    private RoleMappingConfig roleMappingConfig;

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

    public boolean isRoleMappingEnabled() {
        return roleMappingEnabled;
    }

    public void setRoleMappingEnabled(boolean roleMappingEnabled) {
        this.roleMappingEnabled = roleMappingEnabled;
    }

    public String getGroupSearchBase() {
        return this.groupSearchBase;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getRoleProvider() {
        return this.roleProvider;
    }

    public void setRoleProvider(String roleProvider) {
        this.roleProvider = roleProvider;
    }

    @Override
    public RoleMappingConfig getRoleMappingConfig() {
        return roleMappingConfig;
    }

    public void setRoleMappingConfig(RoleMappingConfig roleMappingConfig) {
        this.roleMappingConfig = roleMappingConfig;
    }
}
