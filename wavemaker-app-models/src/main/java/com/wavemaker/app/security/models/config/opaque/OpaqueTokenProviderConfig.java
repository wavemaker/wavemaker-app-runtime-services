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

package com.wavemaker.app.security.models.config.opaque;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;

public class OpaqueTokenProviderConfig extends AbstractProviderConfig {

    public static final String OPAQUE_TOKEN = "OPAQUE_TOKEN";

    @NotBlank
    @ProfilizableProperty("${security.providers.opaqueToken.introspectionUrl}")
    private String introspectionUrl;
    @NotBlank
    @ProfilizableProperty("${security.providers.opaqueToken.clientId}")
    private String clientId;
    @NotBlank
    @ProfilizableProperty("${security.providers.opaqueToken.clientSecret}")
    private String clientSecret;
    @NotBlank
    @ProfilizableProperty("${security.providers.opaqueToken.principalClaimName}")
    private String principalClaimName;

    @ProfilizableProperty("${security.providers.opaqueToken.roleMappingEnabled}")
    private boolean roleMappingEnabled;
    @Valid
    private RoleMappingConfig roleMappingConfig;

    @Override
    public String getType() {
        return OPAQUE_TOKEN;
    }

    public String getIntrospectionUrl() {
        return introspectionUrl;
    }

    public void setIntrospectionUrl(String introspectionUrl) {
        this.introspectionUrl = introspectionUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPrincipalClaimName() {
        return principalClaimName;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        this.principalClaimName = principalClaimName;
    }

    public boolean isRoleMappingEnabled() {
        return roleMappingEnabled;
    }

    public void setRoleMappingEnabled(boolean roleMappingEnabled) {
        this.roleMappingEnabled = roleMappingEnabled;
    }

    @Override
    public RoleMappingConfig getRoleMappingConfig() {
        return roleMappingConfig;
    }

    public void setRoleMappingConfig(RoleMappingConfig roleMappingConfig) {
        this.roleMappingConfig = roleMappingConfig;
    }
}
