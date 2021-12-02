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
package com.wavemaker.runtime.data.replacers.providers;

import org.junit.Test;

import com.wavemaker.commons.util.Tuple;
import com.wavemaker.runtime.commons.variable.VariableType;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 12/7/17
 */
public class VariableTypeHelperTest {

    @Test
    public void fromAppVariableName() {
        final Tuple.Two<VariableType, String> result = VariableTypeHelper
                .fromVariableName("APP_ENVIRONMENT__myProperty__name");
        assertEquals(VariableType.APP_ENVIRONMENT, result.v1);
        assertEquals("myProperty", result.v2);
    }

    @Test
    public void fromServerVariableName() {
        final Tuple.Two<VariableType, String> result = VariableTypeHelper
                .fromVariableName("SERVER__time__name");
        assertEquals(VariableType.SERVER, result.v1);
        assertEquals("time", result.v2);
    }

    @Test
    public void fromPromptVariableName() {
        final Tuple.Two<VariableType, String> result = VariableTypeHelper
                .fromVariableName("name");
        assertEquals(VariableType.PROMPT, result.v1);
        assertEquals("name", result.v2);
    }

}