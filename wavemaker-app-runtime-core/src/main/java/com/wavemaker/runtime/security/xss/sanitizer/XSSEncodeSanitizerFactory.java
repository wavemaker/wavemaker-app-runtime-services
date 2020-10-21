package com.wavemaker.runtime.security.xss.sanitizer;

import com.wavemaker.commons.model.security.XSSConfig;
import com.wavemaker.runtime.WMAppContext;

public class XSSEncodeSanitizerFactory {

    private static final String XSS_CONFIG = "xssConfig";

    private static XSSConfig xssConfig;

    static {
        xssConfig = WMAppContext.getInstance().getSpringBean(XSS_CONFIG);
    }

    public static XSSEncodeSanitizer getInstance() {
        return new XSSEncodeSanitizer(xssConfig.isDataBackwardCompatibility());
    }
}
