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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import com.wavemaker.commons.util.SystemUtils;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;

@Configuration
@Conditional({SecurityEnabledCondition.class, RedisSessionConfigCondition.class})
@ComponentScan("org.springframework.session.data.redis.config.annotation.web.http")
public class RedisSessionConfiguration {
    @Autowired
    private Environment environment;

    @Bean(name = "redisHttpSessionConfiguration")
    public RedisHttpSessionConfiguration redisHttpSessionConfiguration() {
        RedisHttpSessionConfiguration redisHttpSessionConfiguration = new RedisHttpSessionConfiguration();
        redisHttpSessionConfiguration.setMaxInactiveIntervalInSeconds(
            environment.getProperty("security.general.session.timeout", Integer.class) * 60);
        return redisHttpSessionConfiguration;
    }

    @Bean(name = "lettuceConnectionFactory")
    public LettuceConnectionFactory lettuceConnectionFactory() {
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(environment.getProperty("security.session.redis.host"),
            environment.getProperty("security.session.redis.port", Integer.class));
        lettuceConnectionFactory.setDatabase(environment.getProperty("security.session.redis.database", Integer.class));
        lettuceConnectionFactory.setPassword(SystemUtils.decryptIfEncrypted(environment.getProperty("security.session.redis.password")));
        return lettuceConnectionFactory;
    }

    @Bean(name = "sessionRegistry")
    public SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry(
        FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
}
