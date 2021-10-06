package com.wavemaker.runtime.web.listener;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.DispatcherServlet;

import com.wavemaker.commons.web.filter.ThrowableTranslationFilter;
import com.wavemaker.runtime.WMAppContext;
import com.wavemaker.runtime.filter.WMSecurityFilter;
import com.wavemaker.runtime.prefab.web.PrefabControllerServlet;
import com.wavemaker.runtime.service.AppRuntimeService;
import com.wavemaker.runtime.web.servlet.PrefabWebContentServlet;

public class WMApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppRuntimeService appRuntimeService = WMAppContext.getInstance().getSpringBean("appRuntimeService");
        String applicationType = appRuntimeService.getApplicationType();
        ServletContext servletContext = sce.getServletContext();
        registerServlets(servletContext, applicationType);
        registerFilter(servletContext, applicationType);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    private void registerServlets(ServletContext servletContext, String applicationType) {
        ServletRegistration.Dynamic servicesServlet = servletContext.addServlet(
                "services", new DispatcherServlet());
        servicesServlet.setLoadOnStartup(1);
        servicesServlet.setInitParameter("namespace", "project-services");
        servicesServlet.setInitParameter("contextConfigLocation", "");
        servicesServlet.setInitParameter("detectAllHandlerExceptionResolvers", "false");
        servicesServlet.addMapping("/services/*");

        ServletRegistration.Dynamic prefabsServlet = servletContext.addServlet(
                "prefabs", new PrefabControllerServlet());
        prefabsServlet.setLoadOnStartup(1);
        prefabsServlet.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        prefabsServlet.setInitParameter("contextConfigLocation", "com.wavemaker.runtime.prefab.config.PrefabServletConfig");
        prefabsServlet.addMapping("/prefabs/*");

        String prefabWeContentServletName;
        if (applicationType.equals("APPLICATION")) {
            prefabWeContentServletName = "prefabWeContentServlet";
        } else {
            prefabWeContentServletName = "prefabWebContentServlet";
        }
        ServletRegistration.Dynamic prefabWeContentServlet = servletContext.addServlet(
                prefabWeContentServletName, new PrefabWebContentServlet());
        prefabWeContentServlet.addMapping("/app/prefabs/*");

        ServletRegistration.Dynamic cdnFilesServlet = servletContext.addServlet(
                "cdn-files", new DispatcherServlet());
        cdnFilesServlet.setLoadOnStartup(1);
        cdnFilesServlet.setInitParameter("namespace", "project-cdn-files");
        cdnFilesServlet.setInitParameter("contextConfigLocation", "/WEB-INF/cdn-dispatcher-servlet.xml");
        cdnFilesServlet.setInitParameter("detectAllHandlerExceptionResolvers", "false");
        cdnFilesServlet.addMapping("/_cdnUrl_/*");
    }

    private void registerFilter(ServletContext servletContext, String applicationType) {
        FilterRegistration.Dynamic throwableTranslationFilter = servletContext.addFilter("throwableTranslationFilter", new ThrowableTranslationFilter());
        throwableTranslationFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic requestTrackingFilter = servletContext.addFilter("requestTrackingFilter", new DelegatingFilterProxy("requestTrackingFilter"));
        requestTrackingFilter.addMappingForUrlPatterns(null, true, "/*");

        FilterRegistration.Dynamic wmRequestFilter = servletContext.addFilter("wmRequestFilter", new DelegatingFilterProxy("wmRequestFilter"));
        wmRequestFilter.addMappingForUrlPatterns(null, true, "/*");

        FilterRegistration.Dynamic cacheManagementFilter = servletContext.addFilter("cacheManagementFilter", new DelegatingFilterProxy("cacheManagementFilter"));
        cacheManagementFilter.addMappingForUrlPatterns(null, true, "/*");

        FilterRegistration.Dynamic languagePreferenceFilter = servletContext.addFilter("languagePreferenceFilter", new DelegatingFilterProxy("languagePreferenceFilter"));
        languagePreferenceFilter.addMappingForUrlPatterns(null, true, "/index.html");

        FilterRegistration.Dynamic springEncodingFilter = servletContext.addFilter("springEncodingFilter", new CharacterEncodingFilter());
        springEncodingFilter.setInitParameter("encoding", "UTF-8");
        springEncodingFilter.setInitParameter("forceEncoding", "true");
        springEncodingFilter.addMappingForUrlPatterns(null, true, "/*");

        FilterRegistration.Dynamic httpPutFormContentFilter = servletContext.addFilter("HttpPutFormContentFilter", new HttpPutFormContentFilter());
        httpPutFormContentFilter.addMappingForUrlPatterns(null, true, "/*");

        if (applicationType.equals("APPLICATION")) {
            FilterRegistration.Dynamic firewallFilter = servletContext.addFilter("firewallFilter", new DelegatingFilterProxy("firewallFilter"));
            firewallFilter.addMappingForUrlPatterns(null, true, "/*");

            FilterRegistration.Dynamic wmCompressionFilter = servletContext.addFilter("wmCompressionFilter", new DelegatingFilterProxy("wmCompressionFilter"));
            wmCompressionFilter.addMappingForUrlPatterns(null, true, "/*");

            FilterRegistration.Dynamic wmCompositeSecurityFilter = servletContext.addFilter("WMCompositeSecurityFilter", new DelegatingFilterProxy("wmCompositeSecurityFilter"));
            wmCompositeSecurityFilter.addMappingForUrlPatterns(null, true, "/*");

            FilterRegistration.Dynamic springSecurityFilterChain = servletContext.addFilter("springSecurityFilterChain", new WMSecurityFilter());
            springSecurityFilterChain.addMappingForUrlPatterns(null, true, "/*");
        }
    }

}

