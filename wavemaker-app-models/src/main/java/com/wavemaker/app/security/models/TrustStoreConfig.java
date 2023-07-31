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

public class TrustStoreConfig {

    @ProfilizableProperty("${security.general.truststore.config}")
    private TrustStoreConfigType trustStoreConfigType;

    @ProfilizableProperty("${security.general.truststore.file}")
    private String file;

    @ProfilizableProperty("${security.general.truststore.fileType}")
    private String fileType;

    @ProfilizableProperty("${security.general.truststore.password}")
    private String password;

    public TrustStoreConfigType getTrustStoreConfigType() {
        return trustStoreConfigType;
    }

    public void setTrustStoreConfigType(TrustStoreConfigType trustStoreConfigType) {
        this.trustStoreConfigType = trustStoreConfigType;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "TrustStoreConfig{" +
            "trustStoreConfigType=" + trustStoreConfigType +
            ", file='" + file + '\'' +
            ", fileType='" + fileType + '\'' +
            '}';
    }

    public enum TrustStoreConfigType {
        NO_CHECK,
        APPLICATION_ONLY,
        SYSTEM_ONLY,
        APPLICATION_AND_SYSTEM
    }
}
