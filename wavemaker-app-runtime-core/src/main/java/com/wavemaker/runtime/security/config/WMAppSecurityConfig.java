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
package com.wavemaker.runtime.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.wavemaker.app.security.models.CSRFConfig;
import com.wavemaker.app.security.models.LoginConfig;
import com.wavemaker.app.security.models.RememberMeConfig;
import com.wavemaker.app.security.models.RolesConfig;
import com.wavemaker.app.security.models.SSLConfig;
import com.wavemaker.app.security.models.TokenAuthConfig;
import com.wavemaker.app.security.models.XSSConfig;

/**
 * @author Ed Callahan
 *
 * Stores security settings within project-security.xml
 * No logic function here.
 */
public class WMAppSecurityConfig {

    @Value("${security.enabled}")
    private boolean enforceSecurity;

    @Autowired(required = false)
    private LoginConfig loginConfig;

    @Autowired(required = false)
    private RolesConfig rolesConfig;

    @Autowired(required = false)
    private RememberMeConfig rememberMeConfig;

    @Autowired
    private SSLConfig sslConfig;

    @Autowired
    private XSSConfig xssConfig;

    @Autowired(required = false)
    private CSRFConfig csrfConfig;

    @Autowired(required = false)
    private TokenAuthConfig tokenAuthConfig;

    public boolean isEnforceSecurity() {
        return enforceSecurity;
    }

    public void setEnforceSecurity(boolean enforceSecurity) {
        this.enforceSecurity = enforceSecurity;
    }

    public RolesConfig getRolesConfig() {
        return rolesConfig;
    }

    public void setRolesConfig(RolesConfig rolesConfig) {
        this.rolesConfig = rolesConfig;
    }

    public LoginConfig getLoginConfig() {
        return loginConfig;
    }

    public void setLoginConfig(final LoginConfig loginConfig) {
        this.loginConfig = loginConfig;
    }

    public RememberMeConfig getRememberMeConfig() {
        return rememberMeConfig;
    }

    public void setRememberMeConfig(final RememberMeConfig rememberMeConfig) {
        this.rememberMeConfig = rememberMeConfig;
    }

    public SSLConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(final SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    public XSSConfig getXssConfig() {
        return xssConfig;
    }

    public void setXssConfig(final XSSConfig xssConfig) {
        this.xssConfig = xssConfig;
    }

    public CSRFConfig getCsrfConfig() {
        return csrfConfig;
    }

    public void setCsrfConfig(final CSRFConfig csrfConfig) {
        this.csrfConfig = csrfConfig;
    }

    public TokenAuthConfig getTokenAuthConfig() {
        return tokenAuthConfig;
    }

    public void setTokenAuthConfig(final TokenAuthConfig tokenAuthConfig) {
        this.tokenAuthConfig = tokenAuthConfig;
    }

    @Override
    public String toString() {
        return "WMAppSecurityConfig{" +
            "enforceSecurity=" + enforceSecurity +
            ", loginConfig=" + loginConfig +
            ", rememberMeConfig=" + rememberMeConfig +
            ", sslConfig=" + sslConfig +
            ", xssConfig=" + xssConfig +
            ", csrfConfig=" + csrfConfig +
            ", tokenAuthConfig= " + tokenAuthConfig +
            '}';
    }
}
