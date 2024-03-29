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

package com.wavemaker.runtime.security.provider.openid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OpenIdDatabaseRoleMappingCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(OpenIdDatabaseRoleMappingCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String openidActiveRoleProvider = environment.getProperty("security.providers.openId.activeProviders");
        boolean isRoleMappingEnabled = Boolean.TRUE.equals(environment.getProperty("security.providers.openId." + openidActiveRoleProvider
            + ".roleMappingEnabled", Boolean.class));
        String roleProvider = environment.getProperty("security.providers.openId." + openidActiveRoleProvider + ".roleProvider");
        if (isRoleMappingEnabled && roleProvider != null && roleProvider.equals("Database")) {
            logger.info("Initializing Database roleMapping bean for OPENID provider");
            return true;
        }
        return false;
    }
}

