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
package com.wavemaker.runtime.commons.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.springframework.core.env.PropertyResolver;

import com.wavemaker.commons.io.File;
import com.wavemaker.commons.util.PatternMatchingReplaceReader;

/**
 * Created by srujant on 10/10/18.
 */
public class PropertyPlaceHolderReplacementHelper {

    public Reader getPropertyReplaceReader(Reader reader, PropertyResolver propertyResolver) {
        return new PatternMatchingReplaceReader(reader, "${", "}", propertyResolver::getProperty);
    }

    public Reader getPropertyReplaceReader(InputStream inputStream, PropertyResolver propertyResolver) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return getPropertyReplaceReader(inputStreamReader, propertyResolver);
    }

    public Reader getPropertyReplaceReader(File file, PropertyResolver propertyResolver) {
        return getPropertyReplaceReader(file.getContent().asReader(), propertyResolver);
    }
}
