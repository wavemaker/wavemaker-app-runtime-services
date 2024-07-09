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

package com.wavemaker.runtime.security.xss;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class XssCharArrayDeserializer extends StdDeserializer<char[]> {
    public XssCharArrayDeserializer() {
        super(char[].class);
    }

    @Override
    public char[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        XSSSecurityHandler xssSecurityHandler = XSSSecurityHandler.getInstance();
        if (xssSecurityHandler.isInputSanitizationEnabled() && XssContext.isXssEnabled()) {
            return xssSecurityHandler.sanitizeIncomingData(value).toCharArray();
        } else {
            return value.toCharArray();
        }
    }
}
