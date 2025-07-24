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
package com.wavemaker.app.security.models.config.rolemapping;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by jvenugopal on 12-05-2016.
 */
@JsonDeserialize(as = DatabaseRoleMappingConfig.class)
@Schema(title = "DatabaseRoleMappingConfig")
public class DatabaseRoleMappingConfig implements RoleMappingConfig {

    public static final String DB_ROLE = "DATABASE_ROLE";

    @NotBlank
    private String modelName;

    @NotBlank
    private String entityName;

    @NotBlank
    private String tableName;

    @NotBlank
    private String usernameField;

    @NotBlank
    private String usernameColumn;

    private boolean useRolesQuery;

    @NotBlank
    private String roleField;

    @NotBlank
    private String roleColumn;

    private RoleQueryType queryType;
    private String roleQuery;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(String usernameField) {
        this.usernameField = usernameField;
    }

    public String getRoleField() {
        return roleField;
    }

    public void setRoleField(String roleField) {
        this.roleField = roleField;
    }

    public String getUsernameColumn() {
        return usernameColumn;
    }

    public void setUsernameColumn(String usernameColumn) {
        this.usernameColumn = usernameColumn;
    }

    public String getRoleColumn() {
        return roleColumn;
    }

    public void setRoleColumn(String roleColumn) {
        this.roleColumn = roleColumn;
    }

    public boolean isUseRolesQuery() {
        return useRolesQuery;
    }

    public void setUseRolesQuery(boolean useRolesQuery) {
        this.useRolesQuery = useRolesQuery;
    }

    public String getRoleQuery() {
        return roleQuery;
    }

    public void setRoleQuery(String roleQuery) {
        this.roleQuery = roleQuery;
    }

    public RoleQueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(RoleQueryType queryType) {
        this.queryType = queryType;
    }

    @Override
    public String getType() {
        return DB_ROLE;
    }
}
