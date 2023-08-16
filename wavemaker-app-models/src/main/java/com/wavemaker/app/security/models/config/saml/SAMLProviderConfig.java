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
package com.wavemaker.app.security.models.config.saml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;
import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.config.rolemapping.RoleMappingConfig;
import com.wavemaker.app.security.models.saml.MetadataSource;

/**
 * Created by ArjunSahasranam on 17/10/16.
 */
public class SAMLProviderConfig extends AbstractProviderConfig {
    public static final String SAML = "SAML";

    @ProfilizableProperty("${security.providers.saml.idpMetadataSource}")
    private MetadataSource idpMetadataSource; // tells whether idp metadata is loaded from file or url.

    @ProfilizableProperty("${security.providers.saml.idpMetadataUrl}")
    private String idpMetadataUrl;
    private String idpEndpointUrl;
    private String idpPublicKey;
    private boolean createKeystore;
    private String keyStoreLocation; // import

    @ProfilizableProperty("${security.providers.saml.keyStoreFile}")
    private String keyStoreName;

    @ProfilizableProperty("${security.providers.saml.keyStorePassword}")
    private String keyStorePassword;

    @ProfilizableProperty("${security.providers.saml.keyAlias}")
    private String keyAlias;
    private String subjectName; //create
    /**
     * @deprecated
     */
    @Deprecated
    private String entityBaseURL;

    @ProfilizableProperty(value = "${security.providers.saml.roleMappingEnabled}")
    private boolean roleMappingEnabled;

    @ProfilizableProperty("${security.providers.saml.maxAuthenticationAge}")
    private int maxAuthenticationAge = 7200;

    private RoleMappingConfig roleMappingConfig;

    @JsonIgnore
    @ProfilizableProperty("${security.providers.saml.idpMetadataFile:/saml/metadata/idpMetadata.xml}")
    private String idpMetadataFile;

    @JsonIgnore
    @ProfilizableProperty("${security.providers.saml.urlValidateType}")
    private SAMLConfig.ValidateType urlValidateType;

    @Override
    public String getType() {
        return SAML;
    }

    public MetadataSource getIdpMetadataSource() {
        return idpMetadataSource;
    }

    public void setIdpMetadataSource(final MetadataSource idpMetadataSource) {
        this.idpMetadataSource = idpMetadataSource;
    }

    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public void setIdpMetadataUrl(final String idpMetadataUrl) {
        this.idpMetadataUrl = idpMetadataUrl;
    }

    public String getIdpEndpointUrl() {
        return idpEndpointUrl;
    }

    public void setIdpEndpointUrl(final String idpEndpointUrl) {
        this.idpEndpointUrl = idpEndpointUrl;
    }

    public String getIdpPublicKey() {
        return idpPublicKey;
    }

    public void setIdpPublicKey(final String idpPublicKey) {
        this.idpPublicKey = idpPublicKey;
    }

    public boolean isCreateKeystore() {
        return createKeystore;
    }

    public void setCreateKeystore(final boolean createKeystore) {
        this.createKeystore = createKeystore;
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(final String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStoreName() {
        return keyStoreName;
    }

    public void setKeyStoreName(final String keyStoreName) {
        this.keyStoreName = keyStoreName;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(final String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(final String subjectName) {
        this.subjectName = subjectName;
    }

    public String getEntityBaseURL() {
        return entityBaseURL;
    }

    public void setEntityBaseURL(final String entityBaseURL) {
        this.entityBaseURL = entityBaseURL;
    }

    public boolean isRoleMappingEnabled() {
        return roleMappingEnabled;
    }

    public void setRoleMappingEnabled(final boolean roleMappingEnabled) {
        this.roleMappingEnabled = roleMappingEnabled;
    }

    @Override
    public RoleMappingConfig getRoleMappingConfig() {
        return roleMappingConfig;
    }

    public void setRoleMappingConfig(final RoleMappingConfig roleMappingConfig) {
        this.roleMappingConfig = roleMappingConfig;
    }

    public int getMaxAuthenticationAge() {
        return maxAuthenticationAge;
    }

    public void setMaxAuthenticationAge(int maxAuthenticationAge) {
        this.maxAuthenticationAge = maxAuthenticationAge;
    }

    public String getIdpMetadataFile() {
        return idpMetadataFile;
    }

    public void setIdpMetadataFile(String idpMetadataFile) {
        this.idpMetadataFile = idpMetadataFile;
    }

    public SAMLConfig.ValidateType getUrlValidateType() {
        return urlValidateType;
    }

    public void setUrlValidateType(SAMLConfig.ValidateType urlValidateType) {
        this.urlValidateType = urlValidateType;
    }
}
