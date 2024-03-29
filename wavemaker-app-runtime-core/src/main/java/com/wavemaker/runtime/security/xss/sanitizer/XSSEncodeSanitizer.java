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

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

import com.wavemaker.app.security.models.XSSSanitizationLayer;

/**
 * Created by kishorer on 6/7/16.
 */
public class XSSEncodeSanitizer implements XSSSanitizer {

    private static final CharSequenceTranslator UNESCAPE_HTML4 =
        new AggregateTranslator(
            new LookupTranslator(EntityArrays.BASIC_UNESCAPE),
            new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE),
            new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE)
        );

    private static final CharSequenceTranslator ESCAPE_HTML4 =
        new AggregateTranslator(
            new LookupTranslator(EntityArrays.BASIC_ESCAPE),
            new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE),
            new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE)
        );

    private boolean dataPreSanitized;
    private XSSSanitizationLayer xssSanitizationLayer;

    public XSSEncodeSanitizer(boolean dataPreSanitized, XSSSanitizationLayer xssSanitizationLayer) {
        this.dataPreSanitized = dataPreSanitized;
        this.xssSanitizationLayer = xssSanitizationLayer;
    }

    @Override
    public String sanitizeIncomingData(String data) {
        if (data == null) {
            return null;
        }
        return ESCAPE_HTML4.translate(data);
    }

    @Override
    public String sanitizeOutgoingData(String data) {
        if (data == null) {
            return null;
        }
        if (dataPreSanitized || xssSanitizationLayer == XSSSanitizationLayer.BOTH) {
            data = UNESCAPE_HTML4.translate(data);
        }
        return ESCAPE_HTML4.translate(data);
    }
}
