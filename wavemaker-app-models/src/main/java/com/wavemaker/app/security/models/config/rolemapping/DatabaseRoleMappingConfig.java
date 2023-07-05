/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.rolemapping;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by jvenugopal on 12-05-2016.
 */
@JsonDeserialize(as = DatabaseRoleMappingConfig.class)
public class DatabaseRoleMappingConfig implements RoleMappingConfig {

    private String modelName;

    private String entityName;
    private String tableName;

    private String usernameField;
    private String usernameColumn;

    private boolean useRolesQuery;

    private String roleField;
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
}
