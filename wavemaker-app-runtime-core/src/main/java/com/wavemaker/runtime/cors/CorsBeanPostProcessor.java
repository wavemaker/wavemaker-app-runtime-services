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
package com.wavemaker.runtime.cors;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.wavemaker.app.security.models.CorsConfig;
import com.wavemaker.app.security.models.CorsPathEntry;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;

/**
 * Registers all {@link CorsConfig} beans configured in the app using
 * {@link org.springframework.web.cors.UrlBasedCorsConfigurationSource#registerCorsConfiguration(String, CorsConfiguration)}.
 *
 * @author srujant on 5/7/17.
 */
public class CorsBeanPostProcessor implements BeanPostProcessor {

    private static final String DEFAULT_ALLOWED_METHODS = "*";
    private static final String DEFAULT_ALLOWED_HEADERS = "*";
    private static final String DEFAULT_EXPOSED_HEADERS = "";
    private static final long DEFAULT_MAX_AGE = 1600;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof CorsConfig corsConfig) {
            if (corsConfig.isEnabled()) {
                initializeCorsConfiguration(corsConfig);
            }
        }
        return bean;
    }

    private void initializeCorsConfiguration(CorsConfig corsConfig) {
        Long maxAge = corsConfig.getMaxAge();
        boolean allowCredentials = corsConfig.isAllowCredentials();
        for (CorsPathEntry pathEntry : corsConfig.getPathEntries().values()) {
            String path = pathEntry.getPath();
            if (StringUtils.isBlank(path)) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.path.cannot.be.empty"), pathEntry.getName());
            }
            CorsConfiguration corsConfiguration = buildCorsConfigurationObject(pathEntry, maxAge, allowCredentials);
            corsConfiguration.validateAllowCredentials();
            ((UrlBasedCorsConfigurationSource) corsConfigurationSource).registerCorsConfiguration(path, corsConfiguration);
        }
    }

    private CorsConfiguration buildCorsConfigurationObject(CorsPathEntry corsPathEntry, Long maxAge, boolean allowCredentials) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        if (maxAge == null) {
            maxAge = DEFAULT_MAX_AGE;
        }

        String allowedOrigins = corsPathEntry.getAllowedOrigins();
        if (StringUtils.isBlank(allowedOrigins)) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.allowedOrigins.cannot.be.empty"), corsPathEntry.getName());
        }
        corsConfiguration.setMaxAge(maxAge);
        corsConfiguration.setAllowedOrigins(toList(allowedOrigins));
        corsConfiguration.setAllowedMethods(toList(DEFAULT_ALLOWED_METHODS));
        corsConfiguration.setAllowedHeaders(toList(DEFAULT_ALLOWED_HEADERS));
        corsConfiguration.setExposedHeaders(toList(DEFAULT_EXPOSED_HEADERS));
        corsConfiguration.setAllowCredentials(allowCredentials);
        return corsConfiguration;
    }

    private List<String> toList(String inputString) {
        return Arrays.asList(StringUtils.split(inputString, ","));
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
