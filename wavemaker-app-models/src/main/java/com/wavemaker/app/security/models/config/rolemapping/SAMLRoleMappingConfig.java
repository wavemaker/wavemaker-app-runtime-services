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
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * @author Arjun Sahasranam
 */
@JsonDeserialize(as = SAMLRoleMappingConfig.class)
public class SAMLRoleMappingConfig implements RoleMappingConfig {

    @NotBlank
    @ProfilizableProperty("${security.providers.saml.roleAttributeName}")
    private String roleAttrName;

    public String getRoleAttrName() {
        return roleAttrName;
    }

    public void setRoleAttrName(String roleAttrName) {
        this.roleAttrName = roleAttrName;
    }
}