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
package com.wavemaker.app.security.models.config.openid;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;

/**
 * Created by srujant on 30/7/18.
 */
public class OpenIdProviderConfig extends AbstractProviderConfig {

    public static final String OPENID = "OPENID";

    @Valid
    private List<OpenIdProviderInfo> openIdProviderInfoList;
    private boolean roleMappingEnabled;
    private RoleMappingConfig roleMappingConfig;

    @Override
    public String getType() {
        return OPENID;
    }

    public List<OpenIdProviderInfo> getOpenIdProviderInfoList() {
        if (this.openIdProviderInfoList == null) {
            return Collections.emptyList();
        }
        return openIdProviderInfoList;
    }

    public void setOpenIdProviderInfoList(List<OpenIdProviderInfo> openIdProviderInfoList) {
        this.openIdProviderInfoList = openIdProviderInfoList;
    }

    public boolean isRoleMappingEnabled() {
        return roleMappingEnabled;
    }

    public void setRoleMappingEnabled(boolean roleMappingEnabled) {
        this.roleMappingEnabled = roleMappingEnabled;
    }

    public RoleMappingConfig getRoleMappingConfig() {
        return roleMappingConfig;
    }

    public void setRoleMappingConfig(RoleMappingConfig roleMappingConfig) {
        this.roleMappingConfig = roleMappingConfig;
    }
}
