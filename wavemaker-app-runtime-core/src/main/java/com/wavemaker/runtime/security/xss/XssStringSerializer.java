package com.wavemaker.runtime.security.xss;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class XssStringSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        XSSSecurityHandler xssSecurityHandler = XSSSecurityHandler.getInstance();
        gen.writeString(xssSecurityHandler.sanitizeRequestData(value));
    }
}
