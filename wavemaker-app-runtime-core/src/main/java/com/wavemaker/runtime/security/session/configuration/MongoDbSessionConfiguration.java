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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.MongoIndexedSessionRepository;
import org.springframework.session.data.mongo.MongoSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import com.mongodb.MongoCredential;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.SystemUtils;
import com.wavemaker.runtime.core.ApplicationClassLoaderAwareDeserializingConverter;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;

@Configuration
@Conditional({SecurityEnabledCondition.class, MongoDbSessionConfigCondition.class})
public class MongoDbSessionConfiguration {
    @Autowired
    private Environment environment;

    @Bean(name = "mongoCredential")
    public MongoCredential mongoCredentials() {
        return MongoCredential.createScramSha1Credential(environment.getProperty("security.session.mongodb.username"),
            environment.getProperty("security.session.mongodb.dbname"),
            SystemUtils.decryptIfEncrypted(environment.getProperty("security.session.mongodb.password")).toCharArray());
    }

    @Bean(name = "credentialsList")
    public List<MongoCredential> credentialList() {
        List<MongoCredential> mongoCredentialList = new ArrayList<>();
        mongoCredentialList.add(mongoCredentials());
        return mongoCredentialList;
    }

    @Bean(name = "mongoClient")
    public MongoClientFactoryBean mongoClient() {
        MongoClientFactoryBean mongoClientFactoryBean = new MongoClientFactoryBean();
        mongoClientFactoryBean.setHost(environment.getProperty("security.session.mongodb.host"));
        mongoClientFactoryBean.setPort(environment.getProperty("security.session.mongodb.port", Integer.class));
        mongoClientFactoryBean.setCredential(credentialList().toArray(new MongoCredential[0]));
        return mongoClientFactoryBean;
    }

    @Bean(name = "mongoDbFactory")
    public MongoDatabaseFactory mongoDatabaseFactory() {
        try {
            return new SimpleMongoClientDatabaseFactory(mongoClient().getObject(), environment.getProperty("security.session.mongodb.dbname"));
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDatabaseFactory());
    }

    @Bean(name = "jdkMongoSessionConverter")
    public JdkMongoSessionConverter jdkMongoSessionConverter() {
        return new JdkMongoSessionConverter(serializingConverter(), deserializerConverter(), getDuration());
    }

    @Bean(name = "serializingConverter")
    public SerializingConverter serializingConverter() {
        return new SerializingConverter();
    }

    @Bean(name = "deserializingConverter")
    public ApplicationClassLoaderAwareDeserializingConverter deserializerConverter() {
        return new ApplicationClassLoaderAwareDeserializingConverter();
    }

    @Bean(name = "mongoSessionDuration")
    public Duration getDuration() {
        return Duration.ofSeconds(
            environment.getProperty("security.general.session.timeout", Long.class) * 60, 0);
    }

    @Bean(name = "sessionRepository")
    public MongoIndexedSessionRepository sessionRepository() {
        MongoIndexedSessionRepository mongoIndexedSessionRepository = new MongoIndexedSessionRepository(mongoTemplate());
        mongoIndexedSessionRepository.setMongoSessionConverter(jdkMongoSessionConverter());
        mongoIndexedSessionRepository.setMaxInactiveIntervalInSeconds(
            environment.getProperty("security.general.session.timeout", Integer.class) * 60);
        return mongoIndexedSessionRepository;
    }

    @Bean(name = "sessionRegistry")
    public SpringSessionBackedSessionRegistry<MongoSession> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository());
    }
}
