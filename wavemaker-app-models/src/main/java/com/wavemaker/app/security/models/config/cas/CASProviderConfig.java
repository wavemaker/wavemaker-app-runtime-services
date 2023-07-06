/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.cas;

import java.util.List;

import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;

/**
 * @author Arjun Sahasranam
 */
public class CASProviderConfig extends AbstractProviderConfig {

    public static final String CAS = "CAS";

    private String serverUrl;

    private String loginUrl;

    private String validationUrl;

    private String logoutUrl;

    private String serviceParameter;

    private String artifactParameter;

    private boolean roleMappingEnabled;

    private RoleMappingConfig roleMappingConfig;

    private List<String> attributes;

    @Override
    public String getType() {
        return CAS;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(final String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getServiceParameter() {
        return serviceParameter;
    }

    public void setServiceParameter(String serviceParameter) {
        this.serviceParameter = serviceParameter;
    }

    public String getArtifactParameter() {
        return artifactParameter;
    }

    public void setArtifactParameter(String artifactParameter) {
        this.artifactParameter = artifactParameter;
    }

    public boolean isRoleMappingEnabled() {
        return roleMappingEnabled;
    }

    public void setRoleMappingEnabled(boolean roleMappingEnabled) {
        this.roleMappingEnabled = roleMappingEnabled;
    }

    public RoleMappingConfig getRoleMappingConfig() {
        return roleMappingConfig;
    }

    public void setRoleMappingConfig(RoleMappingConfig roleMappingConfig) {
        this.roleMappingConfig = roleMappingConfig;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(final List<String> attributes) {
        this.attributes = attributes;
    }

}
