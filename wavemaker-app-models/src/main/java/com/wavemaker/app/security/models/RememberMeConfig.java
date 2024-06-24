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
package com.wavemaker.app.security.models;

import jakarta.validation.constraints.Min;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * Created by ArjunSahasranam on 22/1/16.
 */
public class RememberMeConfig {

    @ProfilizableProperty("${security.general.rememberMe.enabled}")
    private boolean enabled;

    @Min(1)
    @ProfilizableProperty("${security.general.rememberMe.timeOut}")
    private long tokenValiditySeconds;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public long getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(final long tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    @Override
    public String toString() {
        return "RememberMeConfig{" +
            "enabled=" + enabled +
            ", tokenValiditySeconds=" + tokenValiditySeconds +
            '}';
    }
}
