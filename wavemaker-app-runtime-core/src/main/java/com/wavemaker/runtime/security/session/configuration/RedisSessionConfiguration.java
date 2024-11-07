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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import com.wavemaker.commons.util.SystemUtils;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;

import java.time.Duration;
import java.util.Objects;

@Configuration
@Conditional({SecurityEnabledCondition.class, RedisSessionConfigCondition.class})
@EnableRedisIndexedHttpSession
public class RedisSessionConfiguration {

    @Autowired
    private Environment environment;

    @Bean(name = "redisConnectionFactory")
    public RedisConnectionFactory redisConnectionFactory(RedisConfiguration redisConfiguration) {
        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean(name = "redisConfiguration")
    public RedisConfiguration redisConfiguration() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(Objects.requireNonNull(environment.getProperty("security.session.redis.host")),
            Objects.requireNonNull(environment.getProperty("security.session.redis.port", Integer.class)));
        redisStandaloneConfiguration.setDatabase(Objects.requireNonNull(environment.getProperty("security.session.redis.database", Integer.class)));
        redisStandaloneConfiguration.setPassword(SystemUtils.decryptIfEncrypted(environment.getProperty("security.session.redis.password")));
        return redisStandaloneConfiguration;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(ApplicationContext applicationContext) {
        return new JdkSerializationRedisSerializer(applicationContext.getClassLoader());
    }

    @Bean(name = "sessionRegistry")
    public SessionRegistry sessionRegistry(
        FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        RedisIndexedSessionRepository redisIndexedSessionRepository = (RedisIndexedSessionRepository) sessionRepository;
        redisIndexedSessionRepository
            .setDefaultMaxInactiveInterval(Duration.ofSeconds(Objects.requireNonNull(environment.getProperty("security.general.session.timeout", Integer.class)) * 60L));
        return new SpringSessionBackedSessionRegistry<>(redisIndexedSessionRepository);
    }

}
