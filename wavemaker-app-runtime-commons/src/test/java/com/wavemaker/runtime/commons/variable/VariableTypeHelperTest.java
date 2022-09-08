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
package com.wavemaker.runtime.commons.variable;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 12/7/17
 */
public class VariableTypeHelperTest {

    @Test
    public void fromAppVariableName() {
        final Pair<VariableType, String> result = VariableTypeHelper
            .fromVariableName("APP_ENVIRONMENT__myProperty__name");
        assertEquals(VariableType.APP_ENVIRONMENT, result.getLeft());
        assertEquals("myProperty", result.getRight());
    }

    @Test
    public void fromServerVariableName() {
        final Pair<VariableType, String> result = VariableTypeHelper
            .fromVariableName("SERVER__time__name");
        assertEquals(VariableType.SERVER, result.getLeft());
        assertEquals("time", result.getRight());
    }

    @Test
    public void fromPromptVariableName() {
        final Pair<VariableType, String> result = VariableTypeHelper
            .fromVariableName("name");
        assertEquals(VariableType.PROMPT, result.getLeft());
        assertEquals("name", result.getRight());
    }

}