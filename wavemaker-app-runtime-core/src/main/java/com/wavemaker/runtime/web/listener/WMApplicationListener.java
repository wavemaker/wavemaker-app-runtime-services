package com.wavemaker.runtime.web.listener;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        ServletRegistration.Dynamic servicesServlet = registerServlet(servletContext, "services", new DispatcherServlet());
        servicesServlet.setLoadOnStartup(1);
        servicesServlet.setInitParameter("namespace", "project-services");
        servicesServlet.setInitParameter(CONTEXT_CONFIG_LOCATION, "");
        servicesServlet.setInitParameter("detectAllHandlerExceptionResolvers", "false");
        servicesServlet.addMapping("/services/*");

        ServletRegistration.Dynamic prefabsServlet = registerServlet(servletContext, "prefabs", new PrefabControllerServlet());
        prefabsServlet.setLoadOnStartup(1);
        prefabsServlet.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        prefabsServlet.setInitParameter(CONTEXT_CONFIG_LOCATION, "com.wavemaker.runtime.prefab.config.PrefabServletConfig");
        prefabsServlet.addMapping("/prefabs/*");

        ServletRegistration.Dynamic prefabWebContentServlet = registerServlet(servletContext, "prefabWebContentServlet", new PrefabWebContentServlet());
        prefabWebContentServlet.addMapping("/app/prefabs/*");

        ServletRegistration.Dynamic cdnFilesServlet = registerServlet(servletContext, "cdn-files", new DispatcherServlet());
        cdnFilesServlet.setLoadOnStartup(1);
        cdnFilesServlet.setInitParameter("namespace", "project-cdn-files");
        cdnFilesServlet.setInitParameter(CONTEXT_CONFIG_LOCATION, "/WEB-INF/cdn-dispatcher-servlet.xml");
        cdnFilesServlet.setInitParameter("detectAllHandlerExceptionResolvers", "false");
        cdnFilesServlet.addMapping("/_cdnUrl_/*");
    }

    private void registerFilters(ServletContext servletContext, String applicationType) {
        FilterRegistration.Dynamic springEncodingFilter = registerDelegatingFilterProxyFilter(servletContext, "springEncodingFilter");
        springEncodingFilter.setInitParameter("encoding", "UTF-8");
        springEncodingFilter.setInitParameter("forceEncoding", "true");
        springEncodingFilter.addMappingForUrlPatterns(null, false, "/*");

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

        FilterRegistration.Dynamic httpPutFormContentFilter = registerDelegatingFilterProxyFilter(servletContext, "formContentFilter");
        httpPutFormContentFilter.addMappingForUrlPatterns(null, false, "/*");

        if (applicationType.equals(APPLICATION)) {
            FilterRegistration.Dynamic wmCompressionFilter = registerDelegatingFilterProxyFilter(servletContext, "wmCompressionFilter");
            wmCompressionFilter.addMappingForUrlPatterns(null, false, "/*");
        }

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

        if (RuntimeEnvironment.isTestRunEnvironment()) {
            FilterRegistration.Dynamic htmlFilter = registerDelegatingFilterProxyFilter(servletContext, "cdnUrlReplacementFilter");
            htmlFilter.addMappingForUrlPatterns(null, true, "/*");
        }
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

