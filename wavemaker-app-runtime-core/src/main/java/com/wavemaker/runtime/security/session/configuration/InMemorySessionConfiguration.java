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

package com.wavemaker.runtime.security.session.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.session.WMInMemorySessionRepository;

@Configuration
@Conditional({SecurityEnabledCondition.class, InmemorySessionConfigurationCondition.class})
public class InMemorySessionConfiguration {

    @Autowired
    private Environment environment;

    @Bean(name = "sessionRepository")
    public WMInMemorySessionRepository sessionRepository() {
        WMInMemorySessionRepository wmInMemorySessionRepository = new WMInMemorySessionRepository();
        wmInMemorySessionRepository.setDefaultMaxInactiveInterval(environment.getProperty("security.general.session.timeout", Integer.class) * 60);
        return wmInMemorySessionRepository;
    }

    @Bean(name = "sessionRegistry")
    public SessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository());
    }
}
