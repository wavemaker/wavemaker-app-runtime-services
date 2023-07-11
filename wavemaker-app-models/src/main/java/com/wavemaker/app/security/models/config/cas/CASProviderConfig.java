/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.cas;

import java.util.List;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;

/**
 * @author Arjun Sahasranam
 */
public class CASProviderConfig extends AbstractProviderConfig {

    public static final String CAS = "CAS";

    @ProfilizableProperty("${security.providers.cas.serverUrl}")
    private String serverUrl;

    @ProfilizableProperty("${security.providers.cas.loginUrl}")
    private String loginUrl;

    @ProfilizableProperty("${security.providers.cas.validationUrl}")
    private String validationUrl;

    @ProfilizableProperty("${security.providers.cas.logoutUrl}")
    private String logoutUrl;

    @ProfilizableProperty("${security.providers.cas.serviceParameter}")
    private String serviceParameter;

    @ProfilizableProperty("${security.providers.cas.artifactParameter}")
    private String artifactParameter;

    @ProfilizableProperty(value = "${security.providers.cas.roleMappingEnabled}", isAutoUpdate = true)
    private boolean roleMappingEnabled;

    @ProfilizableProperty(value = "${security.providers.cas.roleProvider}", isAutoUpdate = true)
    private String roleProvider;

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

    public String getRoleProvider() {
        return roleProvider;
    }

    public void setRoleProvider(String roleProvider) {
        this.roleProvider = roleProvider;
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
