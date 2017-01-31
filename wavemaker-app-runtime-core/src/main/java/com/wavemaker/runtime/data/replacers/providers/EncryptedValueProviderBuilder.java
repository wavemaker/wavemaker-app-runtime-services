/**
 * Copyright © 2013 - 2017 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.replacers.providers;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import com.wavemaker.runtime.data.annotations.Encrypted;
import com.wavemaker.runtime.data.replacers.ValueProvider;
import com.wavemaker.runtime.data.replacers.ValueProviderBuilder;
import com.wavemaker.runtime.util.CryptoHelper;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 16/6/16
 */
public class EncryptedValueProviderBuilder implements ValueProviderBuilder {
    @Override
    public ValueProvider build(
            final Field field, final Map<Field, PropertyDescriptor> fieldDescriptorMap, final Annotation annotation) {
        final Encrypted encrypted = (Encrypted) annotation;
        CryptoHelper helper = new CryptoHelper(encrypted.algorithm(), encrypted.key());
        return new EncryptedValueProvider(fieldDescriptorMap.get(field), helper);
    }
}
