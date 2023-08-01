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

package com.wavemaker.app.security.models.config.openid.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ScopesValidator implements ConstraintValidator<ValidateScopes, List<String>> {
    @Override
    public void initialize(ValidateScopes validateScopes) {
    }

    @Override
    public boolean isValid(List<String> scopes, ConstraintValidatorContext constraintValidatorContext) {
        for (String scope : scopes) {
            if (scope != null && scope.contains(" ")) {
                return false;
            }
        }
        return true;
    }
}
