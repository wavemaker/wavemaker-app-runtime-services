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
package com.wavemaker.app.security.models.config.demo;

import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import com.wavemaker.app.security.models.DemoUser;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;

/**
 * @author Ed Callahan
 * @author Frankie Fu
 */
public class DemoProviderConfig extends AbstractProviderConfig {

    public static final String DEMO = "DEMO";

    @Valid
    @NotEmpty
    private List<DemoUser> users;

    @Override
    public String getType() {
        return DEMO;
    }

    public List<DemoUser> getUsers() {
        if (this.users == null) {
            return Collections.emptyList();
        }
        return this.users;
    }

    public void setUsers(List<DemoUser> users) {
        this.users = users;
    }
}
