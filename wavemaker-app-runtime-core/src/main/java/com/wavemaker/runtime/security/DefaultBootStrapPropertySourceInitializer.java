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

package com.wavemaker.runtime.security;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.io.File;
import com.wavemaker.commons.util.PropertiesFileUtils;

//TODO this class right now handles securityService.properties. Ideally there should be common framework to handle bootstrap properties
public class DefaultBootStrapPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBootStrapPropertySourceInitializer.class);

    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        File classPathFile = new ClassPathFile(applicationContext.getClassLoader(), "conf/securityService.properties");
        if (classPathFile.exists()) {
            Properties properties = PropertiesFileUtils.loadProperties(classPathFile);
            applicationContext.getEnvironment().getPropertySources().addLast(new PropertySource<>("securityServicePropertySource") {
                @Override
                public Object getProperty(String name) {
                    if (name != null && name.startsWith("security.")) {
                        DefaultBootStrapPropertySourceInitializer.logger.info("Fetching security services property {}", name);
                        return properties.getProperty(name);
                    }
                    return null;
                }
            });
        }
    }
}
