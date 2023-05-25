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

package com.wavemaker.runtime.security.base.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.wavemaker.commons.model.security.CorsConfig;
import com.wavemaker.commons.model.security.FrameOptions;
import com.wavemaker.commons.model.security.PathEntry;
import com.wavemaker.commons.model.security.SSLConfig;
import com.wavemaker.commons.model.security.XSSConfig;
import com.wavemaker.commons.model.security.XSSFilterStrategy;
import com.wavemaker.commons.model.security.XSSSanitizationLayer;
import com.wavemaker.runtime.cors.CorsBeanPostProcessor;
import com.wavemaker.runtime.security.SecurityService;
import com.wavemaker.runtime.security.config.WMAppSecurityConfig;
import com.wavemaker.runtime.security.controller.SecurityController;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledBaseConfiguration;
import com.wavemaker.runtime.security.filter.WMFrameOptionsHeaderFilter;
import com.wavemaker.runtime.security.filter.WMXContentTypeOptionsFilter;
import com.wavemaker.runtime.security.xss.filter.WMXSSFilter;
import com.wavemaker.runtime.web.filter.ContentSecurityPolicyFilter;
import com.wavemaker.runtime.web.filter.SSLSecureFilter;

@Configuration
@ComponentScan({"com.wavemaker.runtime.security.enabled.configuration", "com.wavemaker.runtime.security.provider.demo",
    "com.wavemaker.runtime.security.provider.database", "com.wavemaker.runtime.security.provider.ad", "com.wavemaker.runtime.security.provider.ldap",
    "com.wavemaker.runtime.security.rememberme.config", "com.wavemaker.runtime.security.session.configuration"})
public class BaseSecurityConfiguration {

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;

    @Bean(name = "securityService")
    public SecurityService securityService() {
        return new SecurityService();
    }

    @Bean(name = "securityController")
    public SecurityController securityController() {
        return new SecurityController();
    }

    @Bean(name = "sslSecureFilter")
    public SSLSecureFilter sslSecureFilter() {
        return new SSLSecureFilter();
    }

    @Bean(name = "wmXSSFilter")
    public WMXSSFilter wmXSSFilter() {
        return new WMXSSFilter();
    }

    @Bean(name = "contentSecurityPolicyFilter")
    public ContentSecurityPolicyFilter contentSecurityPolicyFilter() {
        return new ContentSecurityPolicyFilter();
    }

    @Bean(name = "wmFrameOptionsFilter")
    public WMFrameOptionsHeaderFilter wmFrameOptionsHeaderFilter() {
        WMFrameOptionsHeaderFilter wmFrameOptionsHeaderFilter = new WMFrameOptionsHeaderFilter();
        wmFrameOptionsHeaderFilter.setFrameOptions(frameOptions());
        return wmFrameOptionsHeaderFilter;
    }

    @Bean(name = "frameOptions")
    public FrameOptions frameOptions() {
        FrameOptions frameOptions = new FrameOptions();
        frameOptions.setEnabled(Boolean.parseBoolean(environment.getProperty("security.general.frameOptions.enabled")));
        frameOptions.setMode(FrameOptions.Mode.valueOf(environment.getProperty("security.general.frameOptions.mode")));
        frameOptions.setAllowFromUrl(environment.getProperty("security.general.frameOptions.allowFromUrl"));
        return frameOptions;
    }

    @Bean(name = "wmXContentTypeOptionsFilter")
    public WMXContentTypeOptionsFilter WMXContentTypeOptionsFilter() {
        return new WMXContentTypeOptionsFilter();
    }

    @Bean(name = "corsFilter")
    public CorsFilter corsFilter() {
        return new CorsFilter(urlBasedCorsConfigurationSource());
    }

    @Bean(name = "corsConfigurationSource")
    public UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource() {
        return new UrlBasedCorsConfigurationSource();
    }

    @Bean(name = "corsBeanPostProcessor")
    public CorsBeanPostProcessor corsBeanPostProcessor() {
        return new CorsBeanPostProcessor();
    }

    @Bean(name = "wmCompositeSecurityFilter")
    public FilterChainProxy wmCompositeSecurityFilter() {
        SecurityFilterChain securityFilterChain = new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"), contentSecurityPolicyFilter(), sslSecureFilter(), wmXSSFilter(), wmFrameOptionsHeaderFilter(), WMXContentTypeOptionsFilter(), corsFilter());
        return new FilterChainProxy(securityFilterChain);
    }

    @Bean(name = "WMAppSecurityConfig")
    public WMAppSecurityConfig WMAppSecurityConfig() {
        WMAppSecurityConfig wmAppSecurityConfig = new WMAppSecurityConfig();
        wmAppSecurityConfig.setXssConfig(xssConfig());
        wmAppSecurityConfig.setSslConfig(sslConfig());
        wmAppSecurityConfig.setEnforceSecurity(Boolean.parseBoolean(environment.getProperty("security.enabled")));
        if (environment.getProperty("security.enabled", Boolean.class)) {
            wmAppSecurityConfig.setRolesConfig(securityEnabledBaseConfiguration.rolesConfig());
        }
        return wmAppSecurityConfig;
    }

    @Bean(name = "sslConfig")
    public SSLConfig sslConfig() {
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setSslPort(Integer.parseInt(environment.getProperty("security.general.ssl.port")));
        sslConfig.setUseSSL(Boolean.parseBoolean(environment.getProperty("security.general.ssl.enabled")));
        sslConfig.setExcludedUrls(environment.getProperty("security.general.ssl.excludedUrls"));
        return sslConfig;
    }

    @Bean(name = "xssConfig")
    public XSSConfig xssConfig() {
        XSSConfig xssConfig = new XSSConfig();
        xssConfig.setEnforceXssSecurity(Boolean.parseBoolean(environment.getProperty("security.general.xss.enabled")));
        xssConfig.setDataBackwardCompatibility(Boolean.parseBoolean(environment.getProperty("security.general.xss.dataBackwardCompatibility")));
        xssConfig.setPolicyFile(environment.getProperty("security.general.xss.policyFile"));
        xssConfig.setXssSanitizationLayer(XSSSanitizationLayer.valueOf(environment.getProperty("security.general.xss.sanitizationLayer")));
        xssConfig.setXssFilterStrategy(XSSFilterStrategy.valueOf(environment.getProperty("security.general.xss.filterStrategy")));
        return xssConfig;
    }

    @Bean(name = "corsConfig")
    public CorsConfig corsConfig() {
        CorsConfig corsConfig = new CorsConfig();
        corsConfig.setEnabled(Boolean.parseBoolean(environment.getProperty("security.general.cors.enabled")));
        corsConfig.setMaxAge(Long.parseLong(environment.getProperty("security.general.cors.maxAge")));
        corsConfig.setAllowCredentials(Boolean.parseBoolean(environment.getProperty("security.general.cors.allowCredentials")));
        List<PathEntry> pathEntries = new ArrayList<>();
        pathEntries.add(pathEntry());

        corsConfig.setPathEntries(pathEntries);
        return corsConfig;
    }

    @Bean(name = "root")
    public PathEntry pathEntry() {
        PathEntry rootPathEntry = new PathEntry();
        rootPathEntry.setName("root");
        rootPathEntry.setPath("/**");
        rootPathEntry.setAllowedOrigins(environment.getProperty("security.general.cors.root.allowedOrigins"));
        return rootPathEntry;
    }
}