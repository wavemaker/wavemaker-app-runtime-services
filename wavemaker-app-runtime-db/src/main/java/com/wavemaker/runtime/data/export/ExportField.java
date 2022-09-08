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
package com.wavemaker.runtime.data.export;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 10/12/18
 */
public class ExportField {

    private String displayName;
    private FieldValueProvider valueProvider;

    public ExportField(final String displayName, final FieldValueProvider valueProvider) {
        this.displayName = displayName;
        this.valueProvider = valueProvider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FieldValueProvider getValueProvider() {
        return valueProvider;
    }
}
