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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
@Configuration
@Conditional(RedisSessionConfigCondition.class)
public class RedisSessionConfiguration {

    @Value("${security.general.session.timeout}")
    private int timeOut;

    @Value("${security.session.redis.host}")
    private String redisHost;

    @Value("${security.session.redis.port}")
    private int redisPort;

    @Value("${security.session.redis.database}")
    private int redisDb;

    @Value("${security.session.redis.password}")
    private String password;

    @Bean(name = "redisHttpSessionConfiguration")
    public RedisHttpSessionConfiguration getRedisHttpSessionConfiguration() {
        RedisHttpSessionConfiguration redisHttpSessionConfiguration = new RedisHttpSessionConfiguration();
        redisHttpSessionConfiguration.setMaxInactiveIntervalInSeconds(timeOut * 60);
        return redisHttpSessionConfiguration;
    }

    @Bean(name = "lettuceConnectionFactory")
    public LettuceConnectionFactory getLettuceConnectionFactory() {
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisHost, redisPort);
        lettuceConnectionFactory.setDatabase(redisDb);
        lettuceConnectionFactory.setPassword(password);
        return lettuceConnectionFactory;
    }

//    @Bean(name = "sessionRegistry")
//    public SpringSessionBackedSessionRegistry getSessionRegistry() {
//        return new SpringSessionBackedSessionRegistry<>();
//    }
}
