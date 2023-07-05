/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.oauth;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Uday Shankar
 */
public class OAuthIndividualProviderConfig {

    @NotNull
    @NotBlank
    private String providerId;

    @NotNull
    @NotBlank
    private String appId;

    @NotNull
    @NotBlank
    private String appSecret;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String oAuthProvider) {
        this.providerId = oAuthProvider;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
