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

import java.util.stream.Collectors;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.wavemaker.app.security.models.CorsConfig;
import com.wavemaker.app.security.models.CorsPathEntryConfig;
import com.wavemaker.app.security.models.FrameOptions;
import com.wavemaker.app.security.models.SSLConfig;
import com.wavemaker.app.security.models.XSSConfig;
import com.wavemaker.app.security.models.XSSFilterStrategy;
import com.wavemaker.app.security.models.XSSSanitizationLayer;
import com.wavemaker.runtime.cors.CorsBeanPostProcessor;
import com.wavemaker.runtime.security.SecurityService;
import com.wavemaker.runtime.security.controller.SecurityController;
import com.wavemaker.runtime.security.filter.WMFrameOptionsHeaderFilter;
import com.wavemaker.runtime.security.filter.WMXContentTypeOptionsFilter;
import com.wavemaker.runtime.security.xss.filter.WMXSSFilter;
import com.wavemaker.runtime.web.filter.ContentSecurityPolicyFilter;
import com.wavemaker.runtime.web.filter.SSLSecureFilter;

@Configuration
@EnableConfigurationProperties
public class BaseSecurityConfiguration {

    @Autowired
    private Environment environment;

    @Bean(name = "securityService")
    public SecurityService securityService() {
        return new SecurityService();
    }

    @Bean(name = "securityController")
    public SecurityController securityController() {
        return new SecurityController();
    }

    @Bean(name = "sslSecureFilter")
    public Filter sslSecureFilter() {
        return new SSLSecureFilter();
    }

    @Bean(name = "wmXSSFilter")
    public Filter wmXSSFilter() {
        return new WMXSSFilter();
    }

    @Bean(name = "contentSecurityPolicyFilter")
    public Filter contentSecurityPolicyFilter() {
        return new ContentSecurityPolicyFilter();
    }

    @Bean(name = "wmFrameOptionsFilter")
    public Filter wmFrameOptionsFilter() {
        WMFrameOptionsHeaderFilter wmFrameOptionsHeaderFilter = new WMFrameOptionsHeaderFilter();
        wmFrameOptionsHeaderFilter.setFrameOptions(frameOptions());
        return wmFrameOptionsHeaderFilter;
    }

    @Bean(name = "frameOptions")
    public FrameOptions frameOptions() {
        FrameOptions frameOptions = new FrameOptions();
        frameOptions.setEnabled(environment.getProperty("security.general.frameOptions.enabled", Boolean.class));
        frameOptions.setMode(FrameOptions.Mode.valueOf(environment.getProperty("security.general.frameOptions.mode")));
        frameOptions.setAllowFromUrl(environment.getProperty("security.general.frameOptions.allowFromUrl"));
        return frameOptions;
    }

    @Bean(name = "wmXContentTypeOptionsFilter")
    public Filter wmXContentTypeOptionsFilter() {
        return new WMXContentTypeOptionsFilter();
    }

    @Bean(name = "corsFilter")
    public Filter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean(name = "corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource() {
        return new UrlBasedCorsConfigurationSource();
    }

    @Bean(name = "corsBeanPostProcessor")
    public BeanPostProcessor corsBeanPostProcessor() {
        return new CorsBeanPostProcessor();
    }

    @Bean(name = "wmCompositeSecurityFilter")
    public FilterChainProxy wmCompositeSecurityFilter() {
        SecurityFilterChain securityFilterChain = new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"),
            contentSecurityPolicyFilter(), sslSecureFilter(), wmXSSFilter(), wmFrameOptionsFilter(), wmXContentTypeOptionsFilter(), corsFilter());
        return new FilterChainProxy(securityFilterChain);
    }

    @Bean(name = "sslConfig")
    public SSLConfig sslConfig() {
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setSslPort(environment.getProperty("security.general.ssl.port", Integer.class));
        sslConfig.setUseSSL(environment.getProperty("security.general.ssl.enabled", Boolean.class));
        sslConfig.setExcludedUrls(environment.getProperty("security.general.ssl.excludedUrls"));
        return sslConfig;
    }

    @Bean(name = "xssConfig")
    public XSSConfig xssConfig() {
        XSSConfig xssConfig = new XSSConfig();
        xssConfig.setEnforceXssSecurity(environment.getProperty("security.general.xss.enabled", Boolean.class));
        xssConfig.setDataBackwardCompatibility(environment.getProperty("security.general.xss.dataBackwardCompatibility", Boolean.class));
        xssConfig.setPolicyFile(environment.getProperty("security.general.xss.policyFile"));
        xssConfig.setXssSanitizationLayer(XSSSanitizationLayer.valueOf(environment.getProperty("security.general.xss.sanitizationLayer")));
        xssConfig.setXssFilterStrategy(XSSFilterStrategy.valueOf(environment.getProperty("security.general.xss.filterStrategy")));
        return xssConfig;
    }

    @Bean(name = "corsConfig")
    @ConfigurationProperties(prefix = "security.general.cors")
    public CorsConfig corsConfig() {
        CorsConfig corsConfig = new CorsConfig();
        corsConfig.setEnabled(environment.getProperty("security.general.cors.enabled", Boolean.class));
        corsConfig.setMaxAge(environment.getProperty("security.general.cors.maxAge", Long.class));
        corsConfig.setAllowCredentials(environment.getProperty("security.general.cors.allowCredentials", Boolean.class));
        corsConfig.setPathEntries(corsPathEntryConfig().getPathEntries().values().stream().collect(Collectors.toList()));
        return corsConfig;
    }

    @Bean(name = "corsPathEntryConfig")
    @ConfigurationProperties(prefix = "security.general.cors")
    public CorsPathEntryConfig corsPathEntryConfig() {
        return new CorsPathEntryConfig();
    }
}