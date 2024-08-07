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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class XssStringSerializer extends StdSerializer<String> {

    public XssStringSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        XSSSecurityHandler xssSecurityHandler = XSSSecurityHandler.getInstance();
        if (xssSecurityHandler.isOutputSanitizationEnabled() && XssContext.isXssEnabled()) {
            gen.writeString(xssSecurityHandler.sanitizeOutgoingData(value));
        } else {
            gen.writeString(value);
        }
    }
}