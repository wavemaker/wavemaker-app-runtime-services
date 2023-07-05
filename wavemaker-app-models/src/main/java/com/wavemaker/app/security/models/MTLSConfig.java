/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.security.models;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

public class MTLSConfig {

    @ProfilizableProperty("${security.general.mtls.enabled}")
    private boolean enabled;

    @ProfilizableProperty("${security.general.mtls.keystore.file}")
    private String file;

    @ProfilizableProperty("${security.general.mtls.keystore.fileType}")
    private String fileType;

    @ProfilizableProperty("${security.general.mtls.keystore.password}")
    private String password;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        return "MTLSConfig{" +
            "enabled=" + enabled +
            ", file='" + file + '\'' +
            ", fileType='" + fileType + '\'' +
            '}';
    }
}
