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

package com.wavemaker.runtime.security.provider.openid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.config.openid.OpenIdProviderConfig;
import com.wavemaker.runtime.security.authenticationprovider.WMDelegatingAuthenticationProvider;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.entrypoint.AuthenticationEntryPointRegistry;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.logout.LogoutSuccessHandlerRegistry;
import com.wavemaker.runtime.security.model.AuthProvider;
import com.wavemaker.runtime.security.model.AuthProviderType;
import com.wavemaker.runtime.security.model.ProviderOrder;
import com.wavemaker.runtime.security.provider.authoritiesprovider.OpenidAuthoritiesProviderManager;
import com.wavemaker.runtime.security.provider.openid.handler.WMOpenIdAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.provider.openid.handler.WMOpenIdLogoutSuccessHandler;
import com.wavemaker.runtime.security.utils.SecurityPropertyUtils;

@Configuration
@Conditional({SecurityEnabledCondition.class, OpenIdProviderCondition.class})
public class OpenIdSecurityProviderConfiguration implements WMSecurityConfiguration, BeanFactoryAware {

    private static final String SECURITY_PROVIDERS_OPEN_ID = "security.providers.openId.";

    private BeanFactory beanFactory;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("successHandler")
    @Lazy
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("compositeSessionAuthenticationStrategy")
    @Lazy
    private SessionAuthenticationStrategy compositeSessionAuthenticationStrategy;

    @Autowired
    @Lazy
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private LogoutSuccessHandlerRegistry logoutSuccessHandlerRegistry;

    @Autowired
    private AuthenticationEntryPointRegistry authenticationEntryPointRegistry;

    private Set<AuthProvider> openIdActiveProviders;

    @PostConstruct
    public void init() {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        openIdActiveProviders = SecurityPropertyUtils.getAuthProviderForType(environment, AuthProviderType.OPENID);

        openIdActiveProviders.forEach(authProvider -> {
            AuthenticationEntryPoint openIdAuthenticationEntryPoint = openIdAuthenticationEntryPoint(authProvider.getProviderId());
            String beanName = authProvider.getProviderId() + "OpenIdEntryPoint";
            defaultListableBeanFactory.registerSingleton(beanName, openIdAuthenticationEntryPoint);

            //@PostConstruct is not being called so initializing bean
            defaultListableBeanFactory.initializeBean(openIdAuthenticationEntryPoint, beanName);
            this.authenticationEntryPointRegistry.registerAuthenticationEntryPoint(authProvider, openIdAuthenticationEntryPoint);
        });

        registerOpenidLogoutSuccessHandler();
    }

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return List.of(new SecurityInterceptUrlEntry("/auth/oauth2", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/oauth2/code", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/services/oauth2/**/callback/", Permission.PermitAll));
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterBefore(openIDAuthorizationRequestRedirectFilter(), OAuth2LoginAuthenticationFilter.class);
        http.addFilterAt(openIdLoginAuthenticationFilter(),
            OAuth2LoginAuthenticationFilter.class);
    }

    public AuthenticationEntryPoint openIdAuthenticationEntryPoint(String providerId) {
        OpenIdAuthenticationEntryPoint openIdAuthenticationEntryPoint = new OpenIdAuthenticationEntryPoint();
        openIdAuthenticationEntryPoint.setProviderId(providerId);
        return openIdAuthenticationEntryPoint;
    }

    @Bean(name = "oauth2LoginAuthenticationFilter")
    public Filter openIdLoginAuthenticationFilter() {
        OpenIdLoginAuthenticationFilter authenticationFilter = new OpenIdLoginAuthenticationFilter(clientRegistrationRepository(),
            oAuth2AuthorizedClientService(), "/oauth2/code/*");
        authenticationFilter.setAuthorizationRequestRepository(
            (AuthorizationRequestRepository<OAuth2AuthorizationRequest>) openIDAuthorizationRequestRepository());
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setAuthenticationSuccessHandler(successHandler);
        authenticationFilter.setSessionAuthenticationStrategy(compositeSessionAuthenticationStrategy);
        authenticationFilter.setSecurityContextRepository(securityContextRepository);
        return authenticationFilter;
    }

    @Bean(name = "openidLogoutSuccessHandler")
    public LogoutSuccessHandler wmOpenIdLogoutSuccessHandler() {
        WMOpenIdLogoutSuccessHandler wmOpenIdLogoutSuccessHandler = new WMOpenIdLogoutSuccessHandler();
        wmOpenIdLogoutSuccessHandler.setDefaultTargetUrl("/");
        wmOpenIdLogoutSuccessHandler.setRedirectStrategy(redirectStrategy());
        return wmOpenIdLogoutSuccessHandler;
    }

    @Bean(name = "openidRedirectStrategy")
    public RedirectStrategy redirectStrategy() {
        return new DefaultRedirectStrategy();
    }

    @Bean(name = "oAuth2AuthorizedClientService")
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean(name = "authorizationRequestRedirectFilter")
    public Filter openIDAuthorizationRequestRedirectFilter() {
        OpenIDAuthorizationRequestRedirectFilter openIDAuthorizationRequestRedirectFilter = new OpenIDAuthorizationRequestRedirectFilter(
            clientRegistrationRepository(), "/auth/oauth2");
        openIDAuthorizationRequestRedirectFilter.setAuthorizationRequestRepository(
            (AuthorizationRequestRepository<OAuth2AuthorizationRequest>) openIDAuthorizationRequestRepository());
        return openIDAuthorizationRequestRedirectFilter;
    }

    @Bean(name = "openIDAuthorizationRequestRepository")
    public AuthorizationRequestRepository<? extends OAuth2AuthorizationRequest> openIDAuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean(name = "clientRegistrationRepository")
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryRegistrationRepository();
    }

    @Bean(name = "openIdProviderInfo")
    public List<OpenIdProviderConfig> openIdProviderInfoList() {
        List<OpenIdProviderConfig> openIdProviderInfoList = new ArrayList<>();
        for (AuthProvider authProvider : openIdActiveProviders) {
            String providerId = authProvider.getProviderId();
            OpenIdProviderConfig openIdProviderConfig = new OpenIdProviderConfig();
            openIdProviderConfig.setProviderId(providerId);
            openIdProviderConfig.setClientId(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".clientId"));
            openIdProviderConfig.setClientSecret(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".clientSecret"));
            openIdProviderConfig.setAuthorizationUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".authorizationUrl"));
            openIdProviderConfig.setJwkSetUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".jwkSetUrl"));
            openIdProviderConfig.setLogoutUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".logoutUrl"));
            openIdProviderConfig.setTokenUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".tokenUrl"));
            openIdProviderConfig.setUserInfoUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".userInfoUrl"));
            openIdProviderConfig.setRedirectUrlTemplate("{baseUrl}/oauth2/code/{registrationId}");
            openIdProviderConfig.setUserNameAttributeName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".userNameAttributeName"));
            String scopes = environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + providerId + ".scopes");
            List<String> scopesList = new ArrayList<>();
            if (scopes != null) {
                Collections.addAll(scopesList, scopes.split(","));
            }
            openIdProviderConfig.setScopes(scopesList);
            openIdProviderInfoList.add(openIdProviderConfig);
        }
        return openIdProviderInfoList;
    }

    @Bean(name = "openIdProviderRuntimeConfig")
    public OpenIdProviderRuntimeConfig openIdProviderRuntimeConfig() {
        OpenIdProviderRuntimeConfig openIdProviderRuntimeConfig = new OpenIdProviderRuntimeConfig();
        openIdProviderRuntimeConfig.setOpenIdProviderConfigList(openIdProviderInfoList());
        return openIdProviderRuntimeConfig;
    }

    @Bean(name = "openIdAuthenticationSuccessHandler")
    @Order(0)
    public WMAuthenticationSuccessHandler wmOpenIdAuthenticationSuccessHandler() {
        return new WMOpenIdAuthenticationSuccessHandler();
    }

    @Bean(name = "oAuth2AccessTokenResponseClient")
    public OAuth2AccessTokenResponseClient<? extends AbstractOAuth2AuthorizationGrantRequest> defaultAuthorizationCodeTokenResponseClient() {
        return new DefaultAuthorizationCodeTokenResponseClient();
    }

    @Bean(name = "openIdAuthenticationProvider")
    public AuthenticationProvider oidcAuthorizationCodeAuthenticationProvider(OAuth2AccessTokenResponseClient<? extends AbstractOAuth2AuthorizationGrantRequest>
                                                                                  defaultAuthorizationCodeTokenResponseClient,
                                                                              OAuth2UserService<? extends OAuth2UserRequest, ? extends OAuth2User>
                                                                                  openIdUserService) {
        OidcAuthorizationCodeAuthenticationProvider oidcAuthorizationCodeAuthenticationProvider = new OidcAuthorizationCodeAuthenticationProvider(
            (OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>) defaultAuthorizationCodeTokenResponseClient,
            (OAuth2UserService<OidcUserRequest, OidcUser>) openIdUserService);
        oidcAuthorizationCodeAuthenticationProvider.setJwtDecoderFactory(new OpenIdTokenDecoderFactory());
        return oidcAuthorizationCodeAuthenticationProvider;
    }

    @Bean(name = "openIdDelegatingAuthenticationProvider")
    @Order(ProviderOrder.OPENID_ORDER)
    public WMDelegatingAuthenticationProvider openIdDelegatingAuthenticationProvider(AuthenticationProvider openIdAuthenticationProvider) {
        return new WMDelegatingAuthenticationProvider(openIdAuthenticationProvider, AuthProviderType.OPENID);
    }

    @Bean(name = "openIdUserService")
    public OAuth2UserService<? extends OAuth2UserRequest, ? extends OAuth2User> openIdUserService() {
        return new OpenIdUserService();
    }

    @Bean
    public OpenidAuthoritiesProviderManager openidAuthoritiesProviderManager() {
        return new OpenidAuthoritiesProviderManager();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void registerOpenidLogoutSuccessHandler() {
        this.logoutSuccessHandlerRegistry.registerLogoutSuccessHandler(AuthProviderType.OPENID, wmOpenIdLogoutSuccessHandler());
    }
}
