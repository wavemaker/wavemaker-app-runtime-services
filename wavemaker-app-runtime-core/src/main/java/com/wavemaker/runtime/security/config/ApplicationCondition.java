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

package com.wavemaker.runtime.security.config;

import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.io.File;
import com.wavemaker.commons.util.PropertiesFileUtils;

public class ApplicationCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationCondition.class);
    private static final String APP_PROPERTIES = ".wmproject.properties";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        File appPropertiesFile = new ClassPathFile(context.getClassLoader(), APP_PROPERTIES);
        Properties properties = PropertiesFileUtils.loadFromXml(appPropertiesFile);
        String type = (String) properties.get("type");
        if (Objects.equals(type, "APPLICATION")) {
            logger.info("initializing security beans as project type is APPLICATION");
            return true;
        }
        logger.info("skipping security beans as project type is {}", type);
        return false;
    }
}
