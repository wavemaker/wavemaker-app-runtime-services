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

package com.wavemaker.runtime.security.enabled.configuration;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.wavemaker.app.security.models.LoginConfig;
import com.wavemaker.app.security.models.LoginType;
import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.SessionTimeoutConfig;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.model.FilterInfo;

@Configuration
@Conditional({SecurityEnabledCondition.class, SSOProviderCondition.class})
public class SSOProviderConfiguration implements WMSecurityConfiguration {

    @Autowired
    @Lazy
    private LoginConfig loginConfig;

    @Autowired
    @Lazy
    private SessionTimeoutConfig sessionTimeoutConfig;

    @PostConstruct
    public void init() {
        loginConfig.setType(LoginType.SSO);
        sessionTimeoutConfig.setType(LoginType.SSO);
    }

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return List.of(new SecurityInterceptUrlEntry("/services/security/ssologin", Permission.Authenticated));
    }

    @Override
    public List<FilterInfo> getFilters() {
        //no common Filters
        return List.of();
    }
}
