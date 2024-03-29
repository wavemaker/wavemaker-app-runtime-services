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

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * Created by arjuns on 21/2/17.
 */
public class TokenAuthConfig {

    @ProfilizableProperty(value = "${security.general.tokenService.enabled}", autoUpdate = true)
    private boolean enabled;

    @ProfilizableProperty(value = "${security.general.tokenService.parameter}", autoUpdate = true)
    private String parameter;

    @ProfilizableProperty(value = "${security.general.tokenService.tokenValiditySeconds}", autoUpdate = true)
    private int tokenValiditySeconds;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(final String parameter) {
        this.parameter = parameter;
    }

    public int getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(final int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    @Override
    public String toString() {
        return "TokenAuthConfig{" +
            "enabled=" + enabled +
            ", parameter='" + parameter + '\'' +
            ", tokenValiditySeconds=" + tokenValiditySeconds +
            '}';
    }
}
