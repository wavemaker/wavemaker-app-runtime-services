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

import java.util.Map;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * Created by srujant on 5/7/17.
 */
public class CorsConfig {

    @ProfilizableProperty("${security.general.cors.enabled}")
    private boolean enabled;

    @ProfilizableProperty("${security.general.cors.maxAge}")
    private long maxAge;

    @ProfilizableProperty("${security.general.cors.allowCredentials}")
    private boolean allowCredentials;
    private Map<String, CorsPathEntry> pathEntries;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, CorsPathEntry> getPathEntries() {
        return pathEntries;
    }

    public void setPathEntries(Map<String, CorsPathEntry> pathEntries) {
        this.pathEntries = pathEntries;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
}
