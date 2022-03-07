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
package com.wavemaker.runtime.security.xss.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.wavemaker.commons.model.security.XSSConfig;
import com.wavemaker.commons.model.security.XSSFilterStrategy;
import com.wavemaker.commons.model.security.XSSSanitizationLayer;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.security.config.WMAppSecurityConfig;
import com.wavemaker.runtime.security.xss.sanitizer.DefaultXSSSanitizer;
import com.wavemaker.runtime.security.xss.sanitizer.XSSEncodeSanitizerFactory;
import com.wavemaker.runtime.security.xss.sanitizer.XSSSanitizer;
import com.wavemaker.runtime.security.xss.sanitizer.XSSWhiteListSanitizer;

/**
 * Created by kishorer on 6/7/16.
 */
public class XSSSecurityHandler {

    private static final String WM_APP_SECURITY_CONFIG = "WMAppSecurityConfig";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(XSSSecurityHandler.class);

    private static final XSSSecurityHandler instance = new XSSSecurityHandler();

    private boolean isInitialized;
    private XSSSanitizer xssSanitizer;
    private XSSConfig xssConfig;
    private boolean xssEnabled;

    private XSSSecurityHandler() {
    }

    public static XSSSecurityHandler getInstance() {
        return instance;
    }

    public String sanitizeIncomingData(String data) {
        if (!isInitialized) {
            initConfiguration();
        }
        return xssSanitizer.sanitizeIncomingData(data);
    }

    public String sanitizeOutgoingData(String data) {
        if (!isInitialized) {
            initConfiguration();
        }
        return xssSanitizer.sanitizeOutgoingData(data);
    }

    public boolean isXSSEnabled() {
        if (!isInitialized) {
            initConfiguration();
        }
        return xssEnabled;
    }

    public boolean isInputSanitizationEnabled() {
        if (isXSSEnabled()) {
            XSSSanitizationLayer xssSanitizationLayer = getXSSSanitizationLayer();
            return xssSanitizationLayer == XSSSanitizationLayer.INPUT || xssSanitizationLayer == XSSSanitizationLayer.BOTH;
        }
        return false;
    }

    public boolean isOutputSanitizationEnabled() {
        if (isXSSEnabled()) {
            XSSSanitizationLayer xssSanitizationLayer = getXSSSanitizationLayer();
            return xssSanitizationLayer == XSSSanitizationLayer.OUTPUT || xssSanitizationLayer == XSSSanitizationLayer.BOTH;
        }
        return false;
    }

    private XSSSanitizationLayer getXSSSanitizationLayer() {
        if (!isInitialized) {
            initConfiguration();
        }
        return xssConfig.getXssSanitizationLayer();
    }

    private boolean isXSSEnabled(XSSConfig xssConfig) {
        return xssConfig != null && xssConfig.isEnforceXssSecurity();
    }

    private void initConfiguration() {
        WMAppSecurityConfig wmAppSecurityConfig = getWMAppSecurityConfig();
        if (wmAppSecurityConfig != null) {
            xssConfig = wmAppSecurityConfig.getXssConfig();
            if (isXSSEnabled(xssConfig)) {
                xssEnabled = true;
                buildSanitizer(xssConfig.getXssFilterStrategy());
            } else {
                buildSanitizer(XSSFilterStrategy.NONE);
            }
        } else {
            buildSanitizer(XSSFilterStrategy.NONE);
        }
        isInitialized = true;
    }

    public WMAppSecurityConfig getWMAppSecurityConfig() {
        try {
            return WMAppContext.getInstance().getSpringBean(WM_APP_SECURITY_CONFIG);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.warn("WMAppSecurityConfig bean not found in the application");
            return null;
        }
    }

    private void buildSanitizer(XSSFilterStrategy strategy) {
        switch (strategy) {
            case ENCODE:
                xssSanitizer = XSSEncodeSanitizerFactory.getInstance();
                break;
            case WHITE_LIST:
                xssSanitizer = new XSSWhiteListSanitizer(xssConfig.getPolicyFile());
                break;
            case NONE:
                xssSanitizer = new DefaultXSSSanitizer();
        }
    }
}
