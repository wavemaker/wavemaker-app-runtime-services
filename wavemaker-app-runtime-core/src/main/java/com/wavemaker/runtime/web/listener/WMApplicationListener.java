package com.wavemaker.runtime.web.listener;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.prefab.web.PrefabControllerServlet;
import com.wavemaker.runtime.service.AppRuntimeService;
import com.wavemaker.runtime.web.servlet.PrefabWebContentServlet;

public class WMApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppRuntimeService appRuntimeService = WMAppContext.getInstance().getSpringBean("appRuntimeService");
        String applicationType = appRuntimeService.getApplicationType();
        ServletContext servletContext = sce.getServletContext();
        registerServlets(servletContext);
        registerFilter(servletContext, applicationType);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    private void registerServlets(ServletContext servletContext) {
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

        ServletRegistration.Dynamic prefabWebContentServlet = servletContext.addServlet(
                "prefabWebContentServlet", new PrefabWebContentServlet());
        prefabWebContentServlet.addMapping("/app/prefabs/*");

        ServletRegistration.Dynamic cdnFilesServlet = servletContext.addServlet(
                "cdn-files", new DispatcherServlet());
        cdnFilesServlet.setLoadOnStartup(1);
        cdnFilesServlet.setInitParameter("namespace", "project-cdn-files");
        cdnFilesServlet.setInitParameter("contextConfigLocation", "/WEB-INF/cdn-dispatcher-servlet.xml");
        cdnFilesServlet.setInitParameter("detectAllHandlerExceptionResolvers", "false");
        cdnFilesServlet.addMapping("/_cdnUrl_/*");
    }

    private void registerFilter(ServletContext servletContext, String applicationType) {
        FilterRegistration.Dynamic springEncodingFilter = servletContext.addFilter("springEncodingFilter", new DelegatingFilterProxy("springEncodingFilter"));
        springEncodingFilter.setInitParameter("encoding", "UTF-8");
        springEncodingFilter.setInitParameter("forceEncoding", "true");
        springEncodingFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic throwableTranslationFilter = servletContext.addFilter("throwableTranslationFilter", new DelegatingFilterProxy("throwableTranslationFilter"));
        throwableTranslationFilter.addMappingForUrlPatterns(null, false, "/*");

        if (applicationType.equals("APPLICATION")) {
            FilterRegistration.Dynamic firewallFilter = servletContext.addFilter("firewallFilter", new DelegatingFilterProxy("firewallFilter"));
            firewallFilter.addMappingForUrlPatterns(null, false, "/*");
        }

        FilterRegistration.Dynamic requestTrackingFilter = servletContext.addFilter("requestTrackingFilter", new DelegatingFilterProxy("requestTrackingFilter"));
        requestTrackingFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic wmRequestFilter = servletContext.addFilter("wmRequestFilter", new DelegatingFilterProxy("wmRequestFilter"));
        wmRequestFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic httpPutFormContentFilter = servletContext.addFilter("formContentFilter", new DelegatingFilterProxy("formContentFilter"));
        httpPutFormContentFilter.addMappingForUrlPatterns(null, false, "/*");

        if (applicationType.equals("APPLICATION")) {
            FilterRegistration.Dynamic wmCompressionFilter = servletContext.addFilter("wmCompressionFilter", new DelegatingFilterProxy("wmCompressionFilter"));
            wmCompressionFilter.addMappingForUrlPatterns(null, false, "/*");
        }

        FilterRegistration.Dynamic cacheManagementFilter = servletContext.addFilter("cacheManagementFilter", new DelegatingFilterProxy("cacheManagementFilter"));
        cacheManagementFilter.addMappingForUrlPatterns(null, false, "/*");

        if (applicationType.equals("APPLICATION")) {
            FilterRegistration.Dynamic wmCompositeSecurityFilter = servletContext.addFilter("WMCompositeSecurityFilter", new DelegatingFilterProxy("wmCompositeSecurityFilter"));
            wmCompositeSecurityFilter.addMappingForUrlPatterns(null, true, "/*");

            FilterRegistration.Dynamic springSecurityFilterChain = servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy("springSecurityFilterChain"));
            springSecurityFilterChain.addMappingForUrlPatterns(null, true, "/*");
        }

        if (RuntimeEnvironment.isTestRunEnvironment()) {
            FilterRegistration.Dynamic htmlFilter = servletContext.addFilter("htmlFilter", new DelegatingFilterProxy("htmlFilter"));
            htmlFilter.addMappingForUrlPatterns(null, true, "/index.html");
        }
    }

}

