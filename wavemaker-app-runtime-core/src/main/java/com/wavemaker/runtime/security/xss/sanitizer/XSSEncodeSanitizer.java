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
package com.wavemaker.runtime.security.xss.sanitizer;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import com.wavemaker.commons.model.security.XSSConfig;
import com.wavemaker.runtime.WMAppContext;

/**
 * Created by kishorer on 6/7/16.
 */
public class XSSEncodeSanitizer implements XSSSanitizer {

    private static final String XSS_CONFIG = "xssConfig";

    private static XSSConfig xssConfig;

    static {
        xssConfig = WMAppContext.getInstance().getSpringBean(XSS_CONFIG);
    }

    private static final CharSequenceTranslator UNESCAPE_HTML4 =
            new AggregateTranslator(
                    new LookupTranslator(EntityArrays.BASIC_UNESCAPE()),
                    new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE()),
                    new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE())
            );

    private static final CharSequenceTranslator ESCAPE_HTML4 =
            new AggregateTranslator(
                    new LookupTranslator(EntityArrays.BASIC_ESCAPE()),
                    new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE()),
                    new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE())
            );

    @Override
    public String sanitizeRequestData(String data) {
        if (data == null) {
            return data;
        }
        if (xssConfig.isDataBackwardCompatibility()) {
            data = UNESCAPE_HTML4.translate(data);
        }
        return ESCAPE_HTML4.translate(data);
    }
}
