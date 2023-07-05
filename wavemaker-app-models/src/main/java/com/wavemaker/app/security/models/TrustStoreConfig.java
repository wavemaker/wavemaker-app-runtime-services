/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
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
