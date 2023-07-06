/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
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

    public RoleMappingConfig getRoleMappingConfig() {
        return roleMappingConfig;
    }

    public void setRoleMappingConfig(RoleMappingConfig roleMappingConfig) {
        this.roleMappingConfig = roleMappingConfig;
    }
}
