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

package com.wavemaker.runtime.security.provider.saml;

import java.util.Arrays;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.saml2.provider.service.authentication.logout.OpenSamlLogoutRequestValidator;
import org.springframework.security.saml2.provider.service.authentication.logout.OpenSamlLogoutResponseValidator;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequestValidator;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutResponseValidator;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.saml2.provider.service.web.Saml2WebSsoAuthenticationRequestFilter;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseFilter;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.saml.MetadataSource;
import com.wavemaker.runtime.security.WMApplicationAuthenticationFailureHandler;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.csrf.WMCsrfLogoutHandler;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledBaseConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationRedirectionHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMSamlAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMSamlAuthenticationSuccessRedirectionHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.saml.logout.WMSaml2LogoutRequestFilter;
import com.wavemaker.runtime.security.provider.saml.logout.WMSamlRPInitiatedSuccessHandler;

@Configuration
@Conditional({SecurityEnabledCondition.class, SAMLProviderCondition.class})
public class SAMLSecurityProviderConfiguration implements WMSecurityConfiguration {

    @Autowired
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;

    @Autowired
    private RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    @Qualifier("saml2AuthenticationRequestResolver")
    private Saml2AuthenticationRequestResolver saml2AuthenticationRequestResolver;

    @Autowired
    @Qualifier("openSamlLogoutResponseResolver")
    private Saml2LogoutResponseResolver openSamlLogoutResponseResolver;
    @Autowired
    @Qualifier("saml2LogoutRequestResolver")
    private Saml2LogoutRequestResolver saml2LogoutRequestResolver;

    @Bean(name = "samlConfig")
    public SAMLConfig samlConfig(Environment environment) {
        SAMLConfig samlConfig = new SAMLConfig();
        samlConfig.setValidateType(SAMLConfig.ValidateType.valueOf(environment.getProperty("security.providers.saml.urlValidateType")));
        samlConfig.setIdpMetadataUrl(environment.getProperty("security.providers.saml.idpMetadataUrl"));
        samlConfig.setIdpMetadataFileLocation(environment.getProperty("security.providers.saml.idpMetadataFile"));
        samlConfig.setMetadataSource(MetadataSource.valueOf(environment.getProperty("security.providers.saml.idpMetadataSource")));
        return samlConfig;
    }

    @Bean(name = "wmAuthenticationSuccessRedirectionHandler")
    public WMAuthenticationRedirectionHandler wmAuthenticationSuccessRedirectionHandler() {
        return new WMSamlAuthenticationSuccessRedirectionHandler();
    }

    @Bean(name = "samlFilter")
    public FilterChainProxy samlFilter(Saml2LogoutResponseResolver openSamlLogoutResponseResolver,
                                       SecurityContextLogoutHandler securityContextLogoutHandler, WMCsrfLogoutHandler wmCsrfLogoutHandler,
                                       AuthenticationManager authenticationManager, WMApplicationAuthenticationSuccessHandler successHandler,
                                       WMApplicationAuthenticationFailureHandler failureHandler,
                                       RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new FilterChainProxy(Arrays.asList(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/login/saml2/sso/**"),
                saml2WebSsoAuthenticationFilter(relyingPartyRegistrationResolver, authenticationManager, successHandler, failureHandler)),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml2/service-provider-metadata/**"),
                saml2MetadataFilter(relyingPartyRegistrationResolver)),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml2/authenticate/**"),
                saml2WebSsoAuthenticationRequestFilter(saml2AuthenticationRequestResolver)),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/logout/saml2/slo"),
                saml2LogoutRequestFilter(relyingPartyRegistrationResolver, openSamlLogoutResponseResolver,
                    securityContextLogoutHandler, wmCsrfLogoutHandler), saml2LogoutResponseFilter(relyingPartyRegistrationResolver))));
    }

    @Bean(name = "samlAuthenticationSuccessHandler")
    public WMAuthenticationSuccessHandler samlAuthenticationSuccessHandler() {
        return new WMSamlAuthenticationSuccessHandler();
    }

    @Bean(name = "wmSaml2MetadataResolver")
    public Saml2MetadataResolver wmSaml2MetadataResolver() {
        return new WMSaml2MetadataResolver();
    }

    @Bean(name = "saml2MetadataFilter")
    public Filter saml2MetadataFilter(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new Saml2MetadataFilter(relyingPartyRegistrationResolver, wmSaml2MetadataResolver());
    }

    @Bean(name = "saml2AuthenticationTokenConverter")
    public AuthenticationConverter saml2AuthenticationTokenConverter(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new Saml2AuthenticationTokenConverter(relyingPartyRegistrationResolver);
    }

    @Bean(name = "saml2WebSsoAuthenticationRequestFilter")
    public Filter saml2WebSsoAuthenticationRequestFilter(
        Saml2AuthenticationRequestResolver saml2AuthenticationRequestResolver) {
        return new Saml2WebSsoAuthenticationRequestFilter(saml2AuthenticationRequestResolver);
    }

    @Bean(name = "wmLoginUrlAuthenticationEntryPoint")
    public AuthenticationEntryPoint wmLoginUrlAuthenticationEntryPoint() {
        return new WMSAMLEntryPoint("/saml2/authenticate/saml");
    }

    @Bean(name = "saml2RelyingPartyInitiatedLogoutSuccessHandler")
    public LogoutSuccessHandler saml2RelyingPartyInitiatedLogoutSuccessHandler(Saml2LogoutRequestResolver saml2LogoutRequestResolver) {
        return new WMSamlRPInitiatedSuccessHandler(saml2LogoutRequestResolver);
    }

    @Bean(name = "samlLogoutFilter")
    public LogoutFilter samlLogoutFilter(LogoutHandler securityContextLogoutHandler, LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter = new LogoutFilter(saml2RelyingPartyInitiatedLogoutSuccessHandler(saml2LogoutRequestResolver),
            securityContextLogoutHandler, wmCsrfLogoutHandler);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }

    @Bean(name = "openSamlLogoutResponseValidator")
    public Saml2LogoutResponseValidator openSamlLogoutResponseValidator() {
        return new OpenSamlLogoutResponseValidator();
    }

    @Bean(name = "saml2LogoutResponseFilter")
    public Filter saml2LogoutResponseFilter(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new Saml2LogoutResponseFilter(relyingPartyRegistrationResolver, openSamlLogoutResponseValidator(), logoutSuccessHandler);
    }

    @Bean(name = "logoutFilter")
    public SAMLDelegatingLogoutFilter logoutFilter() {
        SAMLDelegatingLogoutFilter samlDelegatingLogoutFilter = new SAMLDelegatingLogoutFilter();
        samlDelegatingLogoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return samlDelegatingLogoutFilter;
    }

    @Bean(name = "openSamlLogoutRequestValidator")
    public Saml2LogoutRequestValidator getOpenSamlLogoutRequestValidator() {
        return new OpenSamlLogoutRequestValidator();
    }

    @Bean(name = "saml2WebSsoAuthenticationFilter")
    public Filter saml2WebSsoAuthenticationFilter(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver,
                                                  AuthenticationManager authenticationManager,
                                                  WMApplicationAuthenticationSuccessHandler successHandler,
                                                  WMApplicationAuthenticationFailureHandler failureHandler) {
        WMSaml2WebSsoAuthenticationFilter wmSaml2WebSsoAuthenticationFilter = new WMSaml2WebSsoAuthenticationFilter(
            saml2AuthenticationTokenConverter(relyingPartyRegistrationResolver));
        wmSaml2WebSsoAuthenticationFilter.setAuthenticationManager(authenticationManager);
        wmSaml2WebSsoAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        wmSaml2WebSsoAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);
        return wmSaml2WebSsoAuthenticationFilter;
    }

    @Bean(name = "saml2LogoutRequestFilter")
    public Filter saml2LogoutRequestFilter(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver,
                                           Saml2LogoutResponseResolver openSamlLogoutResponseResolver,
                                           SecurityContextLogoutHandler securityContextLogoutHandler,
                                           WMCsrfLogoutHandler wmCsrfLogoutHandler) {
        return new WMSaml2LogoutRequestFilter(relyingPartyRegistrationResolver, getOpenSamlLogoutRequestValidator(),
            openSamlLogoutResponseResolver, securityContextLogoutHandler, wmCsrfLogoutHandler);
    }

    @Bean(name = "authoritiesProvider")
    @Conditional(SAMLRoleProviderCondition.class)
    public AuthoritiesProvider authoritiesProvider(Environment environment, ApplicationContext applicationContext) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHql(Boolean.parseBoolean(environment.getProperty("security.providers.saml.isHQL")));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setRolePrefix("ROLE_");
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty("security.providers.saml.rolesByUsernameQuery"));
        String modelName = environment.getProperty("security.providers.saml.modelName");
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations) applicationContext.getBean(modelName + "Template"));
        defaultAuthoritiesProvider.setTransactionManager(
            (PlatformTransactionManager) applicationContext.getBean(modelName + "TransactionManager"));
        return defaultAuthoritiesProvider;
    }

    @Override
    public void addInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher("/services/security/ssologin")).authenticated();
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAfter(samlFilter(openSamlLogoutResponseResolver,
            (SecurityContextLogoutHandler) securityEnabledBaseConfiguration.securityContextLogoutHandler(),
            (WMCsrfLogoutHandler) securityEnabledBaseConfiguration.wmCsrfLogoutHandler(), securityEnabledBaseConfiguration.authenticationManager(),
            securityEnabledBaseConfiguration.successHandler(),
            securityEnabledBaseConfiguration.failureHandler(),
            relyingPartyRegistrationResolver), RememberMeAuthenticationFilter.class);
    }
}
