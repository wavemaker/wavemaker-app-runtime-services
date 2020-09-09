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
package com.wavemaker.runtime.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.wavemaker.commons.util.PatternMatchingReplaceReader;

/**
 * Created by srujant on 10/10/18.
 */
public class PropertyPlaceHolderReplacementHelper implements EnvironmentAware {

    private Environment environment;

    public Reader getPropertyReplaceReader(Reader reader) {
        return new PatternMatchingReplaceReader(reader, "${", "}", key -> environment.getProperty(key));
    }

    public Reader getPropertyReplaceReader(InputStream inputStream) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        return getPropertyReplaceReader(inputStreamReader);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
