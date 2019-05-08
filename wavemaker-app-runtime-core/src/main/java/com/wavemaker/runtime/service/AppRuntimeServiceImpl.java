package com.wavemaker.runtime.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.i18n.FinalLocaleData;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.util.PropertiesFileUtils;
import com.wavemaker.commons.validations.DbValidationsConstants;
import com.wavemaker.runtime.app.AppFileSystem;
import com.wavemaker.runtime.data.model.DesignServiceResponse;
import com.wavemaker.runtime.data.model.procedures.RuntimeProcedure;
import com.wavemaker.runtime.data.model.queries.RuntimeQuery;
import com.wavemaker.runtime.security.SecurityService;
import com.wavemaker.runtime.util.MultipartQueryUtils;

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

    private String applicationType = null;
    private Map<String, Object> applicationProperties ;

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
                InputStream inputStream = appFileSystem.getClasspathResourceStream(APP_PROPERTIES);
                Properties properties = PropertiesFileUtils.loadFromXml(inputStream);
                applicationProperties = new HashMap<>();
                for (String s : uiProperties) {
                    applicationProperties.put(s, properties.get(s));
                }
                if("APPLICATION".equals(getApplicationType())) {
                    applicationProperties.put("securityEnabled", securityService.isSecurityEnabled());
                    applicationProperties.put("xsrf_header_name", getCsrfHeaderName());
                }
                applicationProperties.put("supportedLanguages", getSupportedLocales(appFileSystem.getWebappI18nLocaleFileNames()));
            }
        }
        return new HashMap<>(applicationProperties);
    }

    public String getApplicationType() {
        synchronized (this) {
            if (applicationType == null) {
                InputStream inputStream = appFileSystem.getClasspathResourceStream(APP_PROPERTIES);
                Properties properties = PropertiesFileUtils.loadFromXml(inputStream);
                applicationType = properties.getProperty("type");
            }
        }
        return applicationType;
    }

    @Override
    public DesignServiceResponse testRunQuery(
            String serviceId, MultipartHttpServletRequest request, Pageable pageable) {
        RuntimeQuery query = MultipartQueryUtils.readContent(request, RuntimeQuery.class);
        MultipartQueryUtils.setMultiparts(query.getParameters(), request.getMultiFileMap());
        return queryDesignService.testRunQuery(serviceId, query, pageable);
    }

    @Override
    public DesignServiceResponse testRunProcedure(String serviceId, RuntimeProcedure procedure) {
        return procedureDesignService.testRunProcedure(serviceId, procedure);
    }

    @Override
    public Object executeQuery(String serviceId, RuntimeQuery query, Pageable pageable) {
        return queryDesignService.executeQuery(serviceId, query, pageable);
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
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
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
