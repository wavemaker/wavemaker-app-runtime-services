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

package com.wavemaker.runtime.security.demo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wavemaker.runtime.security.DemoUserDetailsManager;
import com.wavemaker.runtime.security.WMUser;

@Configuration
@EnableConfigurationProperties
public class DemoRuntimeConfig {

    @Bean(name = "demoConfig")
    @ConfigurationProperties(prefix = "security.providers.demo")
    public DemoConfig demoConfig() {
        return new DemoConfig();
    }

    @Bean(name = "demoUserDetailsService")
    public DemoUserDetailsManager demoUserDetailsService() {
        DemoUserDetailsManager demoUserDetailsManager = new DemoUserDetailsManager();
        List<WMUser> wmUserList = demoConfig().getUsers().stream()
            .map(user -> new WMUser(user.getUserid(), user.getPassword(), user.getRoles())).collect(Collectors.toList());
        demoUserDetailsManager.setUsers(wmUserList);
        return demoUserDetailsManager;
    }

    @Bean(name = "passwordEncoder")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean(name = "demoAuthenticationProvider")
    public DaoAuthenticationProvider demoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(demoUserDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }
}
