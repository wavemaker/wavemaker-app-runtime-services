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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.wavemaker.commons.WMRuntimeException;

public class WMApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(WMApplicationContextInitializer.class);

    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        String bootstrapPropertySourceParam = applicationContext.getServletContext().getInitParameter("bootstrapPropertySource");
        if (bootstrapPropertySourceParam != null) {
            logger.info("Found bootstrapPropertySourceParam param {}", bootstrapPropertySourceParam);
            try {
                Class<?> bootStrapPropertySourceClass = Class.forName(bootstrapPropertySourceParam, true, applicationContext.getClassLoader());
                Object o = BeanUtils.instantiateClass(bootStrapPropertySourceClass);
                if (o instanceof AbstractBootstrapPropertySource) {
                    AbstractBootstrapPropertySource bootstrapPropertySource = (AbstractBootstrapPropertySource) o;
                    bootstrapPropertySource.init(applicationContext);
                } else {
                    throw new WMRuntimeException("Parameter 'bootstrapPropertySource' " + bootstrapPropertySourceParam +
                            " should extend " + AbstractBootstrapPropertySource.class.getName());
                }
            } catch (Exception e) {
                throw new WMRuntimeException("Failed to instantiate bootstrap property source class " + bootstrapPropertySourceParam, e);
            }
        } else {
            logger.debug("bootstrapPropertySourceParam not found");
        }
    }
}