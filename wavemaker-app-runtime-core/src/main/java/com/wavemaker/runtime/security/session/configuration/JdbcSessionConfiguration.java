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

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.WMJdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.web.http.WMJdbcHttpSessionConfiguration;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.session.JdbcSessionScriptInitializer;

@Configuration
@Conditional({SecurityEnabledCondition.class, JdbcSessionConfigCondition.class})
public class JdbcSessionConfiguration {
    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name = "jdbcSessionRepositoryTransactionManager")
    public PlatformTransactionManager jdbcSessionRepositoryTransactionManager() {
        return new DataSourceTransactionManager((DataSource) applicationContext.getBean(
            environment.getProperty("security.session.jdbc.serviceName") + "DataSource"));
    }

    @Bean
    public FindByIndexNameSessionRepository<? extends Session> sessionRepository(@SpringSessionDataSource ObjectProvider<DataSource> springSessionDataSource,
                                                                                 ObjectProvider<DataSource> jdbcSessionPersistenceDataSource,
                                                                                 ObjectProvider<SessionRepositoryCustomizer<WMJdbcIndexedSessionRepository>> sessionRepositoryCustomizers) {
        int maxInactiveIntervalInMinutes = environment.getProperty("security.general.session.timeout", Integer.class, 30);
        int maxInactiveIntervalInSeconds = (int) TimeUnit.SECONDS.convert(maxInactiveIntervalInMinutes, TimeUnit.MINUTES);
        WMJdbcHttpSessionConfiguration wmJdbcHttpSessionConfiguration = new WMJdbcHttpSessionConfiguration();
        wmJdbcHttpSessionConfiguration.setMaxInactiveIntervalInSeconds(maxInactiveIntervalInSeconds);
        wmJdbcHttpSessionConfiguration.setTransactionManager(jdbcSessionRepositoryTransactionManager());
        wmJdbcHttpSessionConfiguration.setSessionRepositoryCustomizer(sessionRepositoryCustomizers);
        wmJdbcHttpSessionConfiguration.setDataSource(springSessionDataSource, jdbcSessionPersistenceDataSource);
        wmJdbcHttpSessionConfiguration.setBeanClassLoader(this.applicationContext.getClassLoader());
        return wmJdbcHttpSessionConfiguration.sessionRepository();
    }

    @Bean(name = "sessionRegistry")
    public SessionRegistry sessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    @Bean(name = "jdbcSessionScriptInitializer")
    public JdbcSessionScriptInitializer jdbcSessionScriptInitializer() {
        return new JdbcSessionScriptInitializer();
    }

    @Bean(name = "jdbcSessionPersistenceDataSource")
    public DataSource dataSource() {
        return (DataSource) applicationContext.getBean(
            environment.getProperty("security.session.jdbc.serviceName") + "DataSource");
    }
}