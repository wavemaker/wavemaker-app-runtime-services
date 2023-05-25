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

package com.wavemaker.app.security.models.jws;

public class JWSProviderConfiguration {
    private boolean enabled;
    private String issuerUrl;
    private String principalClaimName;
    private String roleAttributeName;
    private boolean roleMappingEnabled;
    private String roleProvider;
    private boolean isHQL;
    private String modelName;
    private String rolesByUsernameQuery;

    public boolean getIsHQL() {
        return isHQL;
    }

    public void setIsHQL(boolean isHQL) {
        this.isHQL = isHQL;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    public String getPrincipalClaimName() {
        return principalClaimName;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        this.principalClaimName = principalClaimName;
    }

    public String getRoleAttributeName() {
        return roleAttributeName;
    }

    public void setRoleAttributeName(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }

    public boolean isRoleMappingEnabled() {
        return roleMappingEnabled;
    }

    public void setRoleMappingEnabled(boolean roleMappingEnabled) {
        this.roleMappingEnabled = roleMappingEnabled;
    }

    public String getRoleProvider() {
        return roleProvider;
    }

    public void setRoleProvider(String roleProvider) {
        this.roleProvider = roleProvider;
    }

    public String getRolesByUsernameQuery() {
        return rolesByUsernameQuery;
    }

    public void setRolesByUsernameQuery(String rolesByUsernameQuery) {
        this.rolesByUsernameQuery = rolesByUsernameQuery;
    }
}
