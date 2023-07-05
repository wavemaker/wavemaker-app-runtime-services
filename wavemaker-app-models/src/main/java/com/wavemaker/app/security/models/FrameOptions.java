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
 * Created by srujant on 25/4/17.
 */
public class FrameOptions {

    @ProfilizableProperty("${security.general.frameOptions.allowFromUrl}")
    private String allowFromUrl;

    @ProfilizableProperty("${security.general.frameOptions.enabled}")
    private boolean enabled;

    @ProfilizableProperty("${security.general.frameOptions.mode}")
    private Mode mode;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getAllowFromUrl() {
        return allowFromUrl;
    }

    public void setAllowFromUrl(String allowFromUrl) {
        this.allowFromUrl = allowFromUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "FrameOptions{" +
            "mode=" + mode +
            ", allowFromUrl='" + allowFromUrl + '\'' +
            '}';
    }

    public enum Mode {
        DENY, SAMEORIGIN, ALLOW_FROM;
    }
}
