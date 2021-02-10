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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.i18n.FinalLocaleData;
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
            "timeFormat"};

    private String applicationType;
    private Map<String, Object> applicationProperties;

    @Autowired
    private QueryDesignService queryDesignService;

    @Autowired
    private ProcedureDesignService procedureDesignService;

    @Autowired
    private AppFileSystem appFileSystem;

    @Autowired
    private SecurityService securityService;

    @Override
    public Map<String, Object> getApplicationProperties() {
        synchronized (this) {
            if (applicationProperties == null) {
                URL classpathResourceStream = appFileSystem.getClasspathResource(APP_PROPERTIES);
                Properties properties = PropertiesFileUtils.loadFromURL(classpathResourceStream);
                Map<String, Object> appProperties = new HashMap<>();
                for (String s : uiProperties) {
                    appProperties.put(s, properties.get(s));
                }
                if("APPLICATION".equals(getApplicationType())) {
                    appProperties.put("securityEnabled", securityService.isSecurityEnabled());
                    appProperties.put("xsrf_header_name", getCsrfHeaderName());
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
        synchronized (this) {
            if (applicationType == null) {
                URL classpathResourceStream = appFileSystem.getClasspathResource(APP_PROPERTIES);
                Properties properties = PropertiesFileUtils.loadFromURL(classpathResourceStream);
                applicationType = properties.getProperty("type");
            }
        }
        return applicationType;
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
            FinalLocaleData userLocaleData = JSONUtils.toObject(localeFileInputStream, FinalLocaleData.class);
            map.put(fileName, userLocaleData.getFiles());
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.app.build.filenotfound"));
        }
    }

    private String getCsrfHeaderName() {
        String csrfHeaderName = securityService.getSecurityInfo().getCsrfHeaderName();
        if(csrfHeaderName == null) {
            return "X-WM-XSRF-TOKEN";
        }
        return csrfHeaderName;
    }
}
