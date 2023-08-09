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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;

@Configuration
@Conditional(SSOProviderCondition.class)
public class SSOProviderConfiguration implements WMSecurityConfiguration {
    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return List.of(new SecurityInterceptUrlEntry("/services/security/ssologin", Permission.Authenticated));
    }

    @Override
    public void addFilters(HttpSecurity http) {
        //no common Filters
    }

    @Bean(name = "ssoLoginConfigBeanPostProcessor")
    public SSOLoginConfigBeanPostProcessor ssoLoginConfigBeanPostProcessor() {
        return new SSOLoginConfigBeanPostProcessor();
    }
}
