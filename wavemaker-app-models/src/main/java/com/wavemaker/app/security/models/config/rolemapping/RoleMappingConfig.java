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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by jvenugopal on 12-05-2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ActiveDirectoryRoleMappingConfig.class, name = ActiveDirectoryRoleMappingConfig.AD_ROLE),
    @JsonSubTypes.Type(value = DatabaseRoleMappingConfig.class, name = DatabaseRoleMappingConfig.DB_ROLE),
    @JsonSubTypes.Type(value = RoleAttributeNameMappingConfig.class, name = RoleAttributeNameMappingConfig.ROLE_ATTR_NAME),
    @JsonSubTypes.Type(value = LdapRoleMappingConfig.class, name = LdapRoleMappingConfig.LDAP_ROLE),
})
public interface RoleMappingConfig {
    String getType();
}