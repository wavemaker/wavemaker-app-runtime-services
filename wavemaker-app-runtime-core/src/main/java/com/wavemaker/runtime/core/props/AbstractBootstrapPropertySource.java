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

package com.wavemaker.runtime.core.props;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

public abstract class AbstractBootstrapPropertySource extends PropertySource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBootstrapPropertySource.class);

    private ConfigurableEnvironment environment;

    private ServletContext servletContext;

    public AbstractBootstrapPropertySource() {
        super("applicationBootstrapProperties");
    }

    /**
     * Default implementation adds the dynamic property source as the first element in property sources list.
     * subclasses can override this method for more fine-grained control
     */
    protected void init(ConfigurableWebApplicationContext configurableWebApplicationContext) {
        this.environment = configurableWebApplicationContext.getEnvironment();
        this.servletContext = configurableWebApplicationContext.getServletContext();
        environment.getPropertySources().addFirst(this);
        logger.info("Added new bootstrap property source {} as the first property source", getName());
    }

    protected Environment getEnvironment() {
        return environment;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}