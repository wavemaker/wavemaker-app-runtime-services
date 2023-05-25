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

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.WMJdbcHttpSessionConfiguration;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import com.wavemaker.runtime.security.session.JdbcSessionScriptInitializer;
@Configuration
@Conditional(JdbcSessionConfigCondition.class)
public class JdbcSessionConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    FindByIndexNameSessionRepository sessionRepository;

    @Bean(name = "jdbcSessionRepositoryTransactionManager")
    public DataSourceTransactionManager getJdbcSessionRepositoryTransactionManager() {
        return new DataSourceTransactionManager((DataSource) applicationContext.getBean(environment.getProperty("security.session.jdbc.serviceName") + "Datasource"));
    }

    @Bean(name = "jdbcHttpSessionConfiguration")
    public WMJdbcHttpSessionConfiguration getJdbcHttpSessionConfiguration() {
        WMJdbcHttpSessionConfiguration wmJdbcHttpSessionConfiguration = new WMJdbcHttpSessionConfiguration();
        wmJdbcHttpSessionConfiguration.setMaxInactiveIntervalInSeconds(environment.getProperty("security.general.session.timeout", Integer.class) * 60);
        wmJdbcHttpSessionConfiguration.setTransactionManager(getJdbcSessionRepositoryTransactionManager());
        return wmJdbcHttpSessionConfiguration;
    }

    @Bean(name = "sessionRegistry")
    public SpringSessionBackedSessionRegistry getSessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    @Bean(name = "jdbcSessionScriptInitializer")
    public JdbcSessionScriptInitializer getJdbcSessionScriptInitializer() {
        return new JdbcSessionScriptInitializer();
    }
}