/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.saml;

import com.wavemaker.app.security.models.saml.MetadataSource;

/**
 * Created by ArjunSahasranam on 23/11/16.
 */
public class SAMLConfig {
    private String idpMetadataUrl;
    private String idpMetadataFileLocation;
    private ValidateType validateType;
    private MetadataSource metadataSource;

    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public void setIdpMetadataUrl(final String idpMetadataUrl) {
        this.idpMetadataUrl = idpMetadataUrl;
    }

    public String getIdpMetadataFileLocation() {
        return idpMetadataFileLocation;
    }

    public void setIdpMetadataFileLocation(final String idpMetadataFileLocation) {
        this.idpMetadataFileLocation = idpMetadataFileLocation;
    }

    public ValidateType getValidateType() {
        return validateType;
    }

    public void setValidateType(final ValidateType validateType) {
        this.validateType = validateType;
    }

    public MetadataSource getMetadataSource() {
        return metadataSource;
    }

    public void setMetadataSource(final MetadataSource metadataSource) {
        this.metadataSource = metadataSource;
    }

    public enum ValidateType {
        STRICT,  // String.equals()
        RELAXED, // DEV MODE
        NONE // none
    }
}
