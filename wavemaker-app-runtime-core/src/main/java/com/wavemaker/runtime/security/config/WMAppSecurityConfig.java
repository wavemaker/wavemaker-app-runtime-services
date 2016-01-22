/**
 * Copyright © 2015 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.security.config;

import java.util.Map;

import com.wavemaker.studio.common.model.security.LoginConfig;
import com.wavemaker.studio.common.model.security.RememberMeConfig;
import com.wavemaker.studio.common.model.security.RoleConfig;

/**
 * @author Ed Callahan
 *
 *         Stores security settings within project-security.xml
 *         No logic function here.
 */
public class WMAppSecurityConfig {

    private boolean enforceSecurity;

    private LoginConfig loginConfig;

    private Map<String, RoleConfig> roleMap;

    private RememberMeConfig rememberMeConfig;

    public boolean isEnforceSecurity() {
        return enforceSecurity;
    }

    public void setEnforceSecurity(boolean enforceSecurity) {
        this.enforceSecurity = enforceSecurity;
    }

    public Map<String, RoleConfig> getRoleMap() {
        return roleMap;
    }

    public void setRoleMap(final Map<String, RoleConfig> roleMap) {
        this.roleMap = roleMap;
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

    @Override
    public String toString() {
        return "WMAppSecurityConfig{" +
                "enforceSecurity=" + enforceSecurity +
                ", loginConfig=" + loginConfig +
                ", roleMap=" + roleMap +
                ", rememberMe=" + rememberMeConfig +
                '}';
    }
}
