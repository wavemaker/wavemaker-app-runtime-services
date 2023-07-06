/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.openid;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;
import com.wavemaker.commons.auth.openId.OpenIdProviderInfo;

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
