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

package com.wavemaker.runtime.security.xss.sanitizer;

import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.app.security.models.XSSConfig;

public class XSSEncodeSanitizerFactory {

    private static final String XSS_CONFIG = "xssConfig";

    private static XSSConfig xssConfig;

    static {
        xssConfig = WMAppContext.getInstance().getSpringBean(XSS_CONFIG);
    }

    public static XSSEncodeSanitizer getInstance() {
        return new XSSEncodeSanitizer(xssConfig.isDataBackwardCompatibility(), xssConfig.getXssSanitizationLayer());
    }
}
