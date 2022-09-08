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
package com.wavemaker.runtime.security.openId;

import java.util.List;

import com.wavemaker.commons.auth.openId.OpenIdProviderInfo;

/**
 * Created by srujant on 30/7/18.
 */
public class OpenIdProviderRuntimeConfig {

    private List<OpenIdProviderInfo> openIdProviderInfoList;

    public List<OpenIdProviderInfo> getOpenIdProviderInfoList() {
        return openIdProviderInfoList;
    }

    public void setOpenIdProviderInfoList(List<OpenIdProviderInfo> openIdProviderInfoList) {
        this.openIdProviderInfoList = openIdProviderInfoList;
    }
}
