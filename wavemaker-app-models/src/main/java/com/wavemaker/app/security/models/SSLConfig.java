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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.wavemaker.app.security.models.annotation.NonProfilizableProperty;
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * Created by ArjunSahasranam on 20/6/16.
 */
public class SSLConfig {

    @Min(value = 1)
    @Max(value = 65536)
    @ProfilizableProperty(value = "${security.general.ssl.port}")
    private int sslPort = 443;

    @ProfilizableProperty(value = "${security.general.ssl.enabled}", autoUpdate = true)
    private boolean useSSL;

    @NonProfilizableProperty("${security.general.ssl.excludedUrls:#{null}}")
    private String excludedUrls;

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(final boolean useSSL) {
        this.useSSL = useSSL;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(final int sslPort) {
        this.sslPort = sslPort;
    }

    public String getExcludedUrls() {
        return excludedUrls;
    }

    public void setExcludedUrls(final String excludedUrls) {
        this.excludedUrls = excludedUrls;
    }

    @Override
    public String toString() {
        return "SSLConfig{" +
            "sslPort='" + sslPort + '\'' +
            ", useSSL=" + useSSL +
            '}';
    }
}
