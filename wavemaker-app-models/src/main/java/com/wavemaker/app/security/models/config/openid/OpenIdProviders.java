/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.security.models.config.openid;

import java.util.Map;

public class OpenIdProviders {
    private Map<String, Object> openid;

    public Map<String, Object> getOpenid() {
        return openid;
    }

    public void setOpenid(Map<String, Object> openid) {
        this.openid = openid;
    }
}
