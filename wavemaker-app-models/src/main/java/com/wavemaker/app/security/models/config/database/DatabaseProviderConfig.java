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
package com.wavemaker.app.security.models.config.database;

import jakarta.validation.constraints.NotBlank;

import com.wavemaker.app.security.models.annotation.NonProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Frankie Fu
 */

@Schema(title = "Database Security provider")
public class DatabaseProviderConfig extends AbstractProviderConfig {

    public static final String DATABASE = "DATABASE";

    @NonProfilizableProperty(value = "${security.providers.database.modelName}")
    @NotBlank
    private String modelName;

    @NotBlank
    private String entityName;

    @NotBlank
    private String tableName;

    @NotBlank
    private String unamePropertyName;

    @NotBlank
    private String unameColumnName;

    @NotBlank
    private String uidPropertyName;

    @NotBlank
    private String uidColumnName;

    @NotBlank
    private String pwPropertyName;

    @NotBlank
    private String pwColumnName;

    private String rolePropertyName;

    private String roleColumnName;

    private boolean useRolesQuery;

    @NonProfilizableProperty(value = "${security.providers.database.rolesByUsernameQuery:null}")
    private String rolesByUsernameQuery;

    private String tenantIdField;

    private int defTenantId;

    private String tenantIdPropertyName;

    @NonProfilizableProperty(value = "${security.providers.database.queryType:HQL}")
    private String queryType;

    @NonProfilizableProperty("${security.providers.database.usersByUsernameQuery}")
    private String usersByUsernameQuery;

    @Override
    public String getType() {
        return DATABASE;
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUnamePropertyName() {
        return this.unamePropertyName;
    }

    public void setUnamePropertyName(String unamePropertyName) {
        this.unamePropertyName = unamePropertyName;
    }

    public String getUnameColumnName() {
        return this.unameColumnName;
    }

    public void setUnameColumnName(String unameColumnName) {
        this.unameColumnName = unameColumnName;
    }

    public String getUidPropertyName() {
        return this.uidPropertyName;
    }

    public void setUidPropertyName(String uidPropertyName) {
        this.uidPropertyName = uidPropertyName;
    }

    public String getUidColumnName() {
        return this.uidColumnName;
    }

    public void setUidColumnName(String uidColumnName) {
        this.uidColumnName = uidColumnName;
    }

    public String getPwPropertyName() {
        return this.pwPropertyName;
    }

    public void setPwPropertyName(String pwPropertyName) {
        this.pwPropertyName = pwPropertyName;
    }

    public String getPwColumnName() {
        return this.pwColumnName;
    }

    public void setPwColumnName(String pwColumnName) {
        this.pwColumnName = pwColumnName;
    }

    public String getRolePropertyName() {
        return this.rolePropertyName;
    }

    public void setRolePropertyName(String rolePropertName) {
        this.rolePropertyName = rolePropertName;
    }

    public String getRoleColumnName() {
        return this.roleColumnName;
    }

    public void setRoleColumnName(String roleColumnName) {
        this.roleColumnName = roleColumnName;
    }

    public boolean isUseRolesQuery() {
        return this.useRolesQuery;
    }

    public void setUseRolesQuery(boolean useRolesQuery) {
        this.useRolesQuery = useRolesQuery;
    }

    public String getRolesByUsernameQuery() {
        return this.rolesByUsernameQuery;
    }

    public void setRolesByUsernameQuery(String rolesByUsernameQuery) {
        this.rolesByUsernameQuery = rolesByUsernameQuery;
    }

    public String getTenantIdField() {
        return this.tenantIdField;
    }

    public void setTenantIdField(String val) {
        this.tenantIdField = val;
    }

    public int getDefTenantId() {
        return this.defTenantId;
    }

    public void setDefTenantId(int val) {
        this.defTenantId = val;
    }

    public void setTenantIdPropertyName(String tenantIdPropertyName) {
        this.tenantIdPropertyName = tenantIdPropertyName;
    }

    public String getTenantIdPropertyName() {
        return tenantIdPropertyName;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(final String queryType) {
        this.queryType = queryType;
    }

    public String getUsersByUsernameQuery() {
        return usersByUsernameQuery;
    }

    public void setUsersByUsernameQuery(String usersByUsernameQuery) {
        this.usersByUsernameQuery = usersByUsernameQuery;
    }
}
