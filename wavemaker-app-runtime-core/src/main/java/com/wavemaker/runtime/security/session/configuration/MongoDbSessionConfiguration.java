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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.MongoIndexedSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import com.mongodb.MongoCredential;
import com.wavemaker.runtime.core.ApplicationClassLoaderAwareDeserializingConverter;
@Configuration
@Conditional(MongoDbSessionConfigCondition.class)
public class MongoDbSessionConfiguration {

    @Value("${security.session.mongodb.username}")
    private String userName;

    @Value("${security.session.mongodb.dbname}")
    private String source;

    @Value("${security.session.mongodb.password}")
    private char[] password;

    @Value("${security.session.mongodb.port}")
    private int port;

    @Value("${security.session.mongodb.host}")
    private String host;

    @Value("${security.general.session.timeout}")
    private int timeout;

    @Autowired
    ApplicationContext applicationContext;

    @Bean(name = "mongoCredential")
    public MongoCredential getMongoCredentials() {
        return MongoCredential.createScramSha1Credential(userName, source, password);
    }

    @Bean(name = "credentialsList")
    public List getCredentialList() {
        List<MongoCredential> mongoCredentialList = new ArrayList<>();
        mongoCredentialList.add(getMongoCredentials());
        return mongoCredentialList;
    }

    @Bean(name = "mongoClient")
    public MongoClientFactoryBean getMongoClient() {
        MongoClientFactoryBean mongoClientFactoryBean = new MongoClientFactoryBean();
        mongoClientFactoryBean.setHost(host);
        mongoClientFactoryBean.setPort(port);
        return mongoClientFactoryBean;
    }

    @Bean(name = "jdkMongoSessionConverter")
    public JdkMongoSessionConverter getJdkMongoSessionConverter() {
        return new JdkMongoSessionConverter(getSerializingConverter(), getDeserializerConverter(), getDuration());
    }

    @Bean(name = "serializingConverter")
    public SerializingConverter getSerializingConverter() {
        return new SerializingConverter();
    }

    @Bean(name = "deserializingConverter")
    public ApplicationClassLoaderAwareDeserializingConverter getDeserializerConverter() {
        return new ApplicationClassLoaderAwareDeserializingConverter();
    }

    @Bean(name = "mongoSessionDuration")
    public Duration getDuration() {
        return Duration.ofSeconds(timeout * 60, 0);
    }

    @Bean(name = "sessionRepository")
    public MongoIndexedSessionRepository getSessionRepository() {
        MongoIndexedSessionRepository mongoIndexedSessionRepository = new MongoIndexedSessionRepository((MongoOperations) applicationContext.getBean("mongoTemplate"));
        mongoIndexedSessionRepository.setMongoSessionConverter(getJdkMongoSessionConverter());
        mongoIndexedSessionRepository.setMaxInactiveIntervalInSeconds((timeout * 60));
        return mongoIndexedSessionRepository;
    }

    @Bean(name = "sessionRegistry")
    public SpringSessionBackedSessionRegistry getSessionRegistry() {
        return new SpringSessionBackedSessionRegistry(getSessionRepository());
    }

}
