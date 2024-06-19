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
package com.wavemaker.app.security.models.config.custom;

import jakarta.validation.Valid;

import com.wavemaker.app.security.models.annotation.NonProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;

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
