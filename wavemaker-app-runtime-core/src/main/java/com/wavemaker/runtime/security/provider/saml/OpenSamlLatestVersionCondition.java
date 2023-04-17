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

package com.wavemaker.runtime.security.provider.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OpenSamlLatestVersionCondition implements Condition {

    private static final String OPEN_SAML_VERSION_PROPERTY = "security.providers.saml.useOpenSaml3";
    private static final Logger logger = LoggerFactory.getLogger(OpenSamlLatestVersionCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean useOpenSaml3 = Boolean.parseBoolean(context.getEnvironment().getProperty(OPEN_SAML_VERSION_PROPERTY));
        if (!useOpenSaml3) {
            logger.info("using OpenSaml4 version");
            return true;
        }
        return false;
    }
}
