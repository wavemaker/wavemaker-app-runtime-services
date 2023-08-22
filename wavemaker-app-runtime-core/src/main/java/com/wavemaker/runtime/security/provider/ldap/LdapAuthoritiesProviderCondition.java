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

package com.wavemaker.runtime.security.provider.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.wavemaker.runtime.security.constants.SecurityConstants;

public class LdapAuthoritiesProviderCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(LdapAuthoritiesProviderCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        boolean roleMappingEnabled = Boolean.parseBoolean(environment.getProperty("security.providers.ldap.roleMappingEnabled", String.class));
        String roleProvider = environment.getProperty("security.providers.ldap.roleProvider", String.class);
        if (roleMappingEnabled && SecurityConstants.LDAP_ROLE_PROVIDER.equals(roleProvider)) {
            logger.info("Initializing LDAP RoleMapping beans for LDAP provider");
            return true;
        }
        return false;
    }
}
