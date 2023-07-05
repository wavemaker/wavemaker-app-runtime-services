/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.custom;

import javax.validation.Valid;

import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.annotation.NonProfilizableProperty;

/**
 * Created by venuj on 20-05-2014.
 */
public class CustomProviderConfig extends AbstractProviderConfig {

    public static final String CUSTOM = "CUSTOM";

    @Valid
    @NonProfilizableProperty(value = "${security.providers.custom.class}")
    private String fqCustomAuthenticationManagerClassName;

    @Override
    public String getType() {
        return CUSTOM;
    }

    public String getFqCustomAuthenticationManagerClassName() {
        return fqCustomAuthenticationManagerClassName;
    }

    public void setFqCustomAuthenticationManagerClassName(String fqCustomAuthenticationManagerClassName) {
        this.fqCustomAuthenticationManagerClassName = fqCustomAuthenticationManagerClassName;
    }

}
