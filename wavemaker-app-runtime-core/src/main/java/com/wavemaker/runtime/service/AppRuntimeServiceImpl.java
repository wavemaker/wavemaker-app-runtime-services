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
package com.wavemaker.runtime.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.i18n.LocaleData;
import com.wavemaker.commons.io.File;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.util.PropertiesFileUtils;
import com.wavemaker.commons.validations.DbValidationsConstants;
import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.app.AppFileSystem;
import com.wavemaker.runtime.security.SecurityService;

/**
 * Created by Kishore Routhu on 21/6/17 3:00 PM.
 */
public class AppRuntimeServiceImpl implements AppRuntimeService {

    private static final String APP_PROPERTIES = ".wmproject.properties";

    private String[] uiProperties = {
            "version",
            "defaultLanguage",
            "type",
            "homePage",
            "platformType",
            "activeTheme",
            "displayName",
            "dateFormat",
            "timeFormat",
            "preferBrowserLang"};

    private Map<String, Object> applicationProperties;

    @Autowired
    private AppFileSystem appFileSystem;

    @Autowired
    private SecurityService securityService;

    @Override
    public Map<String, Object> getApplicationProperties() {
        synchronized (this) {
            if (applicationProperties == null) {
                File appPropertiesFile = appFileSystem.getClassPathFile(APP_PROPERTIES);
                Properties properties = PropertiesFileUtils.loadFromXml(appPropertiesFile);
                Map<String, Object> appProperties = new HashMap<>();
                for (String s : uiProperties) {
                    appProperties.put(s, properties.get(s));
                }
                if("APPLICATION".equals(getApplicationType(appProperties))) {
                    appProperties.put("securityEnabled", securityService.isSecurityEnabled());
                    appProperties.put("xsrf_header_name", securityService.getSecurityInfo().getCsrfHeaderName());
                    appProperties.put("xsrf_cookie_name", securityService.getSecurityInfo().getCsrfCookieName());
                }
                appProperties
                        .put("supportedLanguages", getSupportedLocales(appFileSystem.getWebappI18nLocaleFileNames()));
                appProperties.put("isTestRuntime", RuntimeEnvironment.isTestRunEnvironment());
                this.applicationProperties = appProperties;
            }
        }
        return new HashMap<>(applicationProperties);
    }

    public String getApplicationType() {
        return getApplicationType(getApplicationProperties());
    }

    private String getApplicationType(Map<String, Object> appProperties) {
        return (String) appProperties.get("type");
    }

    @Override
    public InputStream getValidations(HttpServletResponse httpServletResponse) {
        return appFileSystem.getWebappResource("WEB-INF/" + DbValidationsConstants.DB_VALIDATIONS_JSON_FILE);
    }

    private Map<String, Object> getSupportedLocales(Set<String> localeFileNames) {
        Map<String, Object> localeMap = new HashMap<>();
        localeFileNames.forEach(localeName -> addToLocaleMap(localeMap, appFileSystem.getWebappResource(localeName), localeName));
        return localeMap;
    }

    private void addToLocaleMap(Map<String, Object> map, InputStream localeFileInputStream, String fileName) {
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));
        try {
            LocaleData userLocaleData = JSONUtils.toObject(localeFileInputStream, LocaleData.class);
            map.put(fileName, userLocaleData.getFiles());
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.app.build.filenotfound"));
        }
    }
}
