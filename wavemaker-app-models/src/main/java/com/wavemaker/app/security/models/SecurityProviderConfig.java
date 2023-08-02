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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wavemaker.app.security.models.config.ad.ActiveDirectoryProviderConfig;
import com.wavemaker.app.security.models.config.cas.CASProviderConfig;
import com.wavemaker.app.security.models.config.custom.CustomProviderConfig;
import com.wavemaker.app.security.models.config.database.DatabaseProviderConfig;
import com.wavemaker.app.security.models.config.demo.DemoProviderConfig;
import com.wavemaker.app.security.models.config.jws.JWSProviderConfig;
import com.wavemaker.app.security.models.config.ldap.LdapProviderConfig;
import com.wavemaker.app.security.models.config.opaque.OpaqueTokenProviderConfig;
import com.wavemaker.app.security.models.config.openid.OpenIdProviderConfig;
import com.wavemaker.app.security.models.config.saml.SAMLProviderConfig;

/**
 * Created by venuj on 19-05-2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ActiveDirectoryProviderConfig.class, name = ActiveDirectoryProviderConfig.DIRECTORY),
    @JsonSubTypes.Type(value = CASProviderConfig.class, name = CASProviderConfig.CAS),
    @JsonSubTypes.Type(value = CustomProviderConfig.class, name = CustomProviderConfig.CUSTOM),
    @JsonSubTypes.Type(value = DatabaseProviderConfig.class, name = DatabaseProviderConfig.DATABASE),
    @JsonSubTypes.Type(value = DemoProviderConfig.class, name = DemoProviderConfig.DEMO),
    @JsonSubTypes.Type(value = LdapProviderConfig.class, name = LdapProviderConfig.LDAP),
    @JsonSubTypes.Type(value = OpenIdProviderConfig.class, name = OpenIdProviderConfig.OPENID),
    @JsonSubTypes.Type(value = SAMLProviderConfig.class, name = SAMLProviderConfig.SAML),
    @JsonSubTypes.Type(value = JWSProviderConfig.class, name = JWSProviderConfig.JWS),
    @JsonSubTypes.Type(value = OpaqueTokenProviderConfig.class, name = OpaqueTokenProviderConfig.OPAQUE_TOKEN),
})
public interface SecurityProviderConfig {
    String getType();

    boolean isEnabled();
}
