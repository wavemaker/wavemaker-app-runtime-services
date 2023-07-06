/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.security.models.config.jws;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.wavemaker.app.security.models.config.AbstractProviderConfig;

public class JWSProviderConfig extends AbstractProviderConfig {

    public static final String JWS = "JWS";

    @Valid
    private Map<String, JWSProviderInfo> jwsProviderIdVsProviderInfo;

    @Override
    public String getType() {
        return JWS;
    }

    public Map<String, JWSProviderInfo> getJwsProviderIdVsProviderInfo() {
        if (jwsProviderIdVsProviderInfo == null) {
            jwsProviderIdVsProviderInfo = new HashMap<>();
        }
        return jwsProviderIdVsProviderInfo;
    }

    public void setJwsProviderIdVsProviderInfo(Map<String, JWSProviderInfo> jwsProviderIdVsProviderInfo) {
        this.jwsProviderIdVsProviderInfo = jwsProviderIdVsProviderInfo;
    }
}
