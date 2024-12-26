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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.wavemaker.runtime.security.utils.SecurityPropertyUtils;

import static com.wavemaker.runtime.security.constants.SecurityConstants.OPENID_PROVIDER;

public class OpenIdProviderCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(OpenIdProviderCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Set<String> activeProviderTypes = SecurityPropertyUtils.getActiveProviderTypes(context.getEnvironment());
        if (activeProviderTypes.contains(OPENID_PROVIDER)) {
            logger.info("Initializing OPENID beans as OPENID is selected as active security provider");
            return true;
        }
        return false;
    }
}
