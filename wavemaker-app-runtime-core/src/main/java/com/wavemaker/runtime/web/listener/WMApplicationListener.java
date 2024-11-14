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

package com.wavemaker.runtime.web.listener;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.prefab.web.PrefabControllerServlet;
import com.wavemaker.runtime.security.config.WMAppSecurityConfig;
import com.wavemaker.runtime.service.AppRuntimeService;
import com.wavemaker.runtime.web.servlet.PrefabWebContentServlet;

public class WMApplicationListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(WMApplicationListener.class);

    private static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private static final String APPLICATION = "APPLICATION";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppRuntimeService appRuntimeService = WMAppContext.getInstance().getSpringBean("appRuntimeService");
        String applicationType = appRuntimeService.getApplicationType();
        ServletContext servletContext = sce.getServletContext();
        registerServlets(servletContext);
        registerFilters(servletContext, applicationType);
    }

    private void registerServlets(ServletContext servletContext) {
        Environment environment = WMAppContext.getInstance().getSpringBean(Environment.class);
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement("",
            environment.getProperty("app.multipartconfig.maxFileSize", Long.class, 300000000L),
            environment.getProperty("app.multipartconfig.maxRequestSize", Long.class, -1L), 0);

        ServletRegistration.Dynamic servicesServlet = registerServlet(servletContext, "services", new DispatcherServlet());
        servicesServlet.setLoadOnStartup(1);
        servicesServlet.setInitParameter("namespace", "project-services");
        servicesServlet.setInitParameter(CONTEXT_CONFIG_LOCATION, "");
        servicesServlet.setInitParameter("detectAllHandlerExceptionResolvers", "false");
        servicesServlet.setMultipartConfig(multipartConfigElement);
        servicesServlet.addMapping("/services/*");

        ServletRegistration.Dynamic prefabsServlet = registerServlet(servletContext, "prefabs", new PrefabControllerServlet());
        prefabsServlet.setLoadOnStartup(1);
        prefabsServlet.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        prefabsServlet.setInitParameter(CONTEXT_CONFIG_LOCATION, "com.wavemaker.runtime.prefab.config.PrefabServletConfig");
        prefabsServlet.addMapping("/prefabs/*");
        prefabsServlet.setMultipartConfig(multipartConfigElement);

        ServletRegistration.Dynamic prefabWebContentServlet = registerServlet(servletContext, "prefabWebContentServlet", new PrefabWebContentServlet());
        prefabWebContentServlet.addMapping("/app/prefabs/*");
    }

    private void registerFilters(ServletContext servletContext, String applicationType) {

        FilterRegistration.Dynamic throwableTranslationFilter = registerDelegatingFilterProxyFilter(servletContext, "throwableTranslationFilter");
        throwableTranslationFilter.addMappingForUrlPatterns(null, false, "/*");

        if (applicationType.equals(APPLICATION)) {
            FilterRegistration.Dynamic firewallFilter = registerDelegatingFilterProxyFilter(servletContext, "firewallFilter");
            firewallFilter.addMappingForUrlPatterns(null, false, "/*");
        }

        FilterRegistration.Dynamic requestTrackingFilter = registerDelegatingFilterProxyFilter(servletContext, "requestTrackingFilter");
        requestTrackingFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic wmRequestFilter = registerDelegatingFilterProxyFilter(servletContext, "wmRequestFilter");
        wmRequestFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic multipartFilter = registerDelegatingFilterProxyFilter(servletContext, "multipartFilter");
        multipartFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic httpPutFormContentFilter = registerDelegatingFilterProxyFilter(servletContext, "formContentFilter");
        httpPutFormContentFilter.addMappingForUrlPatterns(null, false, "/*");

        if (applicationType.equals(APPLICATION)) {
            FilterRegistration.Dynamic wmCompressionFilter = registerDelegatingFilterProxyFilter(servletContext, "wmCompressionFilter");
            wmCompressionFilter.addMappingForUrlPatterns(null, false, "/*");
        }

        FilterRegistration.Dynamic springEncodingFilter = registerDelegatingFilterProxyFilter(servletContext, "springEncodingFilter");
        springEncodingFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic cacheManagementFilter = registerDelegatingFilterProxyFilter(servletContext, "cacheManagementFilter");
        cacheManagementFilter.addMappingForUrlPatterns(null, false, "/*");

        if (applicationType.equals(APPLICATION)) {
            FilterRegistration.Dynamic wmCompositeSecurityFilter = registerDelegatingFilterProxyFilter(servletContext, "wmCompositeSecurityFilter");
            wmCompositeSecurityFilter.addMappingForUrlPatterns(null, true, "/*");

            FilterRegistration.Dynamic springSecurityFilterChain;
            WMAppSecurityConfig wmAppSecurityConfig = WMAppContext.getInstance().getSpringBean(WMAppSecurityConfig.class);
            if (wmAppSecurityConfig.isEnforceSecurity()) {
                String securityFilterBeanToRegister;
                if (RuntimeEnvironment.isTestRunEnvironment()) {
                    securityFilterBeanToRegister = "skipSupportedSecurityFilter";
                } else {
                    securityFilterBeanToRegister = "springSecurityFilterChain";
                }
                springSecurityFilterChain = registerDelegatingFilterProxyFilter(servletContext, securityFilterBeanToRegister);
                springSecurityFilterChain.addMappingForUrlPatterns(null, true, "/*");
            }
        }

        FilterRegistration.Dynamic cdnUrlReplacementFilter = registerDelegatingFilterProxyFilter(servletContext, "cdnUrlReplacementFilter");
        cdnUrlReplacementFilter.addMappingForUrlPatterns(null, true, "/*");

        FilterRegistration.Dynamic activeThemeFilter = registerDelegatingFilterProxyFilter(servletContext, "activeThemeReplacementFilter");
        activeThemeFilter.addMappingForUrlPatterns(null, true, "/*");

    }

    private FilterRegistration.Dynamic registerFilter(ServletContext servletContext, String filterName, Filter filter) {
        logger.info("Registering filter : {} ", filterName);
        return servletContext.addFilter(filterName, filter);
    }

    private ServletRegistration.Dynamic registerServlet(ServletContext servletContext, String servletName, Servlet servlet) {
        logger.info("Registering servlet : {} ", servletName);
        return servletContext.addServlet(servletName, servlet);
    }

    private FilterRegistration.Dynamic registerDelegatingFilterProxyFilter(ServletContext servletContext, String filterName) {
        return registerFilter(servletContext, filterName, new DelegatingFilterProxy(filterName));
    }

}

