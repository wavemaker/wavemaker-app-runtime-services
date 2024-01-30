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

import javax.servlet.Filter;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.wavemaker.app.security.models.CorsConfig;
import com.wavemaker.app.security.models.FrameOptions;
import com.wavemaker.app.security.models.SSLConfig;
import com.wavemaker.app.security.models.XSSConfig;
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
    public Filter wmFrameOptionsFilter(FrameOptions frameOptions) {
        WMFrameOptionsHeaderFilter wmFrameOptionsHeaderFilter = new WMFrameOptionsHeaderFilter();
        wmFrameOptionsHeaderFilter.setFrameOptions(frameOptions);
        return wmFrameOptionsHeaderFilter;
    }

    @Bean(name = "frameOptions")
    public FrameOptions frameOptions() {
        return new FrameOptions();
    }

    @Bean(name = "wmXContentTypeOptionsFilter")
    public Filter wmXContentTypeOptionsFilter() {
        return new WMXContentTypeOptionsFilter();
    }

    @Bean(name = "corsFilter")
    public Filter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
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
    public FilterChainProxy wmCompositeSecurityFilter(Filter sslSecureFilter, Filter wmXSSFilter, Filter contentSecurityPolicyFilter,
                                                      Filter wmFrameOptionsFilter, Filter wmXContentTypeOptionsFilter, Filter corsFilter) {
        SecurityFilterChain securityFilterChain = new DefaultSecurityFilterChain(new AntPathRequestMatcher("/**"),
            contentSecurityPolicyFilter, sslSecureFilter, wmXSSFilter, wmFrameOptionsFilter, wmXContentTypeOptionsFilter, corsFilter);
        return new FilterChainProxy(securityFilterChain);
    }

    @Bean(name = "sslConfig")
    public SSLConfig sslConfig() {
        return new SSLConfig();
    }

    @Bean(name = "xssConfig")
    public XSSConfig xssConfig() {
        return new XSSConfig();
    }

    @Bean(name = "corsConfig")
    @ConfigurationProperties(prefix = "security.general.cors")
    public CorsConfig corsConfig() {
        return new CorsConfig();
    }
}