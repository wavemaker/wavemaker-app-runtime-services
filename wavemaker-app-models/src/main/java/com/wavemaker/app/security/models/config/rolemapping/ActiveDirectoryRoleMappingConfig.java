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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonDeserialize(as = ActiveDirectoryRoleMappingConfig.class)
@Schema(title = "ActiveDirectoryRoleMappingConfig")
public class ActiveDirectoryRoleMappingConfig implements RoleMappingConfig {

    public static final String ROLE_MAPPING_AD = "ROLE_MAPPING_AD";
    private String groupRoleAttribute;

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    @Override
    public String getType() {
        return ROLE_MAPPING_AD;
    }
}
