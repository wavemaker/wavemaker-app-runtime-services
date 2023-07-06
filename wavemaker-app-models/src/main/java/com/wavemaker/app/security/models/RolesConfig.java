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
package com.wavemaker.app.security.models;

import java.util.Map;
import java.util.Objects;

/**
 * @author Uday Shankar
 */
public class RolesConfig {

    private Map<String, RoleConfig> roleMap;

    public Map<String, RoleConfig> getRoleMap() {
        return roleMap;
    }

    public void setRoleMap(Map<String, RoleConfig> roleMap) {
        this.roleMap = roleMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RolesConfig that = (RolesConfig) o;
        return Objects.equals(roleMap, that.roleMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleMap);
    }

}
