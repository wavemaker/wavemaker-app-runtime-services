/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.rolemapping;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by jvenugopal on 12-05-2016.
 */
@JsonDeserialize(as = RoleAttributeNameMappingConfig.class)
public class RoleAttributeNameMappingConfig implements RoleMappingConfig {

    @NotBlank
    private String roleAttributeName;

    public String getRoleAttributeName() {
        return roleAttributeName;
    }

    public void setRoleAttributeName(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }
}