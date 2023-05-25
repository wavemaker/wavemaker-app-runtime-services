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

import java.util.Objects;

/**
 * Created by Arjun Sahasranam on 30/6/15.
 */
public class Role {

    private String name;
    private String description;
    private final RoleConfig roleConfig;

    public Role() {
        this.roleConfig = new RoleConfig();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoleConfig getRoleConfig() {
        return roleConfig;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Role role = (Role) o;
        return Objects.equals(name, role.name) &&
            Objects.equals(roleConfig, role.roleConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, roleConfig);
    }

    @Override
    public String toString() {
        return "Role{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", roleConfig=" + roleConfig +
            '}';
    }
}
