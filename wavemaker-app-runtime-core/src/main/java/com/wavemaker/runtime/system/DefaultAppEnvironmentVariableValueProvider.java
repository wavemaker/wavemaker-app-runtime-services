/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 10/7/17
 */
@Component
public class DefaultAppEnvironmentVariableValueProvider extends AbstractVariableValueProvider implements AppEnvironmentVariableValueProvider {

    private static final String APP_ENVIRONMENT_PROPERTY_PREFIX = "app.environment.";

    private final Environment environment;

    @Autowired
    public DefaultAppEnvironmentVariableValueProvider(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object getValue(final String variableName) {
        if (environment.containsProperty(APP_ENVIRONMENT_PROPERTY_PREFIX + variableName)) {
            return environment.getProperty(APP_ENVIRONMENT_PROPERTY_PREFIX + variableName);
        }
        throw new IllegalArgumentException("Environment property not found with key:" + variableName);
    }
}
