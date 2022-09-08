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
package com.wavemaker.runtime.rest.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.wavemaker.runtime.commons.variable.VariableType;
import com.wavemaker.runtime.rest.RestConstants;
import com.wavemaker.tools.apidocs.tools.core.model.VendorUtils;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.Parameter;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 31/5/19
 */
public class RestRequestUtils {

    private static final List<String> VAR_TYPES = Arrays.asList(VariableType.SERVER.name(),
        VariableType.APP_ENVIRONMENT.name());

    public static Optional<String> findVariableValue(Parameter parameter) {
        final String variableType = (String) VendorUtils.getWMExtension(parameter, RestConstants.VARIABLE_TYPE);
        final String variableValue = (String) VendorUtils.getWMExtension(parameter, RestConstants.VARIABLE_KEY);
        if (StringUtils.isNotBlank(variableType) && StringUtils.isNotBlank(variableValue)) {
            final String value = VariableType.valueOf(variableType.toUpperCase()).getValue(variableValue, String.class);
            return Optional.ofNullable(value);
        }

        return Optional.empty();
    }

    public static boolean isVariableDefined(Parameter parameter) {
        final String variableType = (String) VendorUtils.getWMExtension(parameter, RestConstants.VARIABLE_TYPE);
        final String variableValue = (String) VendorUtils.getWMExtension(parameter, RestConstants.VARIABLE_KEY);
        return StringUtils.isNotBlank(variableType) && VAR_TYPES.contains(variableType) && StringUtils
            .isNotBlank(variableValue);
    }
}
