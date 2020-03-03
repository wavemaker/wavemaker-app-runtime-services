package com.wavemaker.runtime.security.xss;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class XssEscapeModule extends SimpleModule {
    public XssEscapeModule() {
        super("XssEscapeModule", new Version(8, 2, 0, null, null, null));
        addSerializer(String.class, new XssStringSerializer());
    }
}
