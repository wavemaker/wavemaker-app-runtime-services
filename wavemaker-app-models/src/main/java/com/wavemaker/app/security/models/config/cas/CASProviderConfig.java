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

    @ProfilizableProperty(value = "${security.providers.cas.roleMappingEnabled}")
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

    @Override
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
