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
package com.wavemaker.runtime;

import java.util.Properties;

import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.util.PropertiesFileUtils;

/**
 * Created by srujant on 29/12/16.
 */
public class AppRuntimeProperties {

    private AppRuntimeProperties(){}

    private static Properties properties;

    static {
        ClassPathFile classPathFile = new ClassPathFile(AppRuntimeProperties.class.getClassLoader(), "app.properties");
        properties = PropertiesFileUtils.loadProperties(classPathFile);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }


}
