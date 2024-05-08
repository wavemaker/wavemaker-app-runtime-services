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

package com.wavemaker.runtime.security.provider.cas.proxy;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import com.wavemaker.app.security.models.config.cas.CASProviderConfig;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.cas.CASPGTEnableCondition;
import com.wavemaker.runtime.security.provider.cas.CASSecurityProviderCondition;
import com.wavemaker.runtime.security.provider.cas.filter.WMCasAuthenticationFilter;

@Configuration
@Conditional({SecurityEnabledCondition.class, CASSecurityProviderCondition.class, CASPGTEnableCondition.class})
public class CASProxyGrantingTicketConfiguration {

    @Bean(name = "cas20ProxyTicketValidator")
    public TicketValidator cas20ProxyTicketValidator(CASProviderConfig casProviderConfig, ProxyGrantingTicketStorage proxyGrantingTicketStorage,
                                                     HttpURLConnectionFactory casUrlConnectionFactory, Cas20ProxyRetriever cas20ProxyRetriever) {
        Cas20ProxyTicketValidator cas20ProxyTicketValidator = new Cas20ProxyTicketValidator(casProviderConfig.getServerUrl());
        cas20ProxyTicketValidator.setAcceptAnyProxy(true);
        cas20ProxyTicketValidator.setProxyGrantingTicketStorage(proxyGrantingTicketStorage);
        cas20ProxyTicketValidator.setProxyRetriever(cas20ProxyRetriever);
        cas20ProxyTicketValidator.setURLConnectionFactory(casUrlConnectionFactory);
        return cas20ProxyTicketValidator;
    }

    @Bean(name = "cas20ProxyRetriever")
    public Cas20ProxyRetriever cas20ProxyRetriever(CASProviderConfig casProviderConfig, HttpURLConnectionFactory casUrlConnectionFactory) {
        return new Cas20ProxyRetriever(casProviderConfig.getServerUrl(), "UTF-8", casUrlConnectionFactory);
    }

    @Bean(name = "proxyGrantingTicketStorage")
    public ProxyGrantingTicketStorage proxyGrantingTicketStorage() {
        return new ProxyGrantingTicketStorageImpl();
    }

    @Bean(name = "casFilter")
    public Filter casFilter(AuthenticationDetailsSource<HttpServletRequest, ?> WMWebAuthenticationDetailsSource, ServiceProperties casServiceProperties,
                            ProxyGrantingTicketStorage proxyGrantingTicketStorage, @Lazy AuthenticationSuccessHandler successHandler,
                            @Lazy AuthenticationFailureHandler failureHandler, @Lazy AuthenticationManager authenticationManager,
                            @Lazy SessionAuthenticationStrategy compositeSessionAuthenticationStrategy) {

        WMCasAuthenticationFilter casAuthenticationFilter = new WMCasAuthenticationFilter();
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        casAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);
        casAuthenticationFilter.setAuthenticationManager(authenticationManager);
        casAuthenticationFilter.setAuthenticationDetailsSource(WMWebAuthenticationDetailsSource);
        casAuthenticationFilter.setServiceProperties(casServiceProperties);
        casAuthenticationFilter.setSessionAuthenticationStrategy(compositeSessionAuthenticationStrategy);
        casAuthenticationFilter.setProxyGrantingTicketStorage(proxyGrantingTicketStorage);
        casAuthenticationFilter.setProxyReceptorUrl("/j_spring_cas_security_proxyreceptor");
        return casAuthenticationFilter;
    }
}
