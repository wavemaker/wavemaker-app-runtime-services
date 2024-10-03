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
import java.util.Objects;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
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
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.config.openid.OpenIdProviderInfo;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;
import com.wavemaker.runtime.security.provider.openid.handler.WMOpenIdAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.provider.openid.handler.WMOpenIdLogoutSuccessHandler;

@Configuration
@Conditional({SecurityEnabledCondition.class, OpenIdProviderCondition.class})
public class OpenIdSecurityProviderConfiguration implements WMSecurityConfiguration {

    private static final String SECURITY_PROVIDERS_OPEN_ID = "security.providers.openId.";
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

    @Value("${security.providers.openId.activeProviders}")
    private String openIdActiveProvider;

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

    @Bean(name = "openIdEntryPoint")
    public AuthenticationEntryPoint openIdAuthenticationEntryPoint() {
        OpenIdAuthenticationEntryPoint openIdAuthenticationEntryPoint = new OpenIdAuthenticationEntryPoint();
        openIdAuthenticationEntryPoint.setProviderId(openIdActiveProvider);
        return openIdAuthenticationEntryPoint;
    }

    @Bean(name = "oauth2LoginAuthenticationFilter")
    public Filter openIdLoginAuthenticationFilter() {
        OpenIdLoginAuthenticationFilter authenticationFilter = new OpenIdLoginAuthenticationFilter(inMemoryRegistrationRepository(),
            inMemoryOAuth2AuthorizedClientService(), "/oauth2/code/*");
        authenticationFilter.setAuthorizationRequestRepository(
            (AuthorizationRequestRepository<OAuth2AuthorizationRequest>) openIDAuthorizationRequestRepository());
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setAuthenticationSuccessHandler(successHandler);
        authenticationFilter.setSessionAuthenticationStrategy(compositeSessionAuthenticationStrategy);
        authenticationFilter.setSecurityContextRepository(securityContextRepository);
        return authenticationFilter;
    }

    @Bean(name = "logoutSuccessHandler")
    public LogoutSuccessHandler wmOpenIdLogoutSuccessHandler(RedirectStrategy redirectStrategy) {
        WMOpenIdLogoutSuccessHandler wmOpenIdLogoutSuccessHandler = new WMOpenIdLogoutSuccessHandler();
        wmOpenIdLogoutSuccessHandler.setDefaultTargetUrl("/");
        wmOpenIdLogoutSuccessHandler.setRedirectStrategy(redirectStrategy);
        return wmOpenIdLogoutSuccessHandler;
    }

    @Bean(name = "inMemoryOAuth2AuthorizedClientService")
    public OAuth2AuthorizedClientService inMemoryOAuth2AuthorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(inMemoryRegistrationRepository());
    }

    @Bean(name = "authorizationRequestRedirectFilter")
    public Filter openIDAuthorizationRequestRedirectFilter() {
        OpenIDAuthorizationRequestRedirectFilter openIDAuthorizationRequestRedirectFilter = new OpenIDAuthorizationRequestRedirectFilter(
            inMemoryRegistrationRepository(), "/auth/oauth2");
        openIDAuthorizationRequestRedirectFilter.setAuthorizationRequestRepository(
            (AuthorizationRequestRepository<OAuth2AuthorizationRequest>) openIDAuthorizationRequestRepository());
        return openIDAuthorizationRequestRedirectFilter;
    }

    @Bean(name = "openIDAuthorizationRequestRepository")
    public AuthorizationRequestRepository<? extends OAuth2AuthorizationRequest> openIDAuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean(name = "inMemoryRegistrationRepository")
    public ClientRegistrationRepository inMemoryRegistrationRepository() {
        return new InMemoryRegistrationRepository();
    }

    @Bean(name = "openIdProviderInfo")
    public OpenIdProviderInfo openIdProviderInfo() {
        OpenIdProviderInfo openIdProviderInfo = new OpenIdProviderInfo();
        openIdProviderInfo.setProviderId(openIdActiveProvider);
        openIdProviderInfo.setClientId(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".clientId"));
        openIdProviderInfo.setClientSecret(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".clientSecret"));
        openIdProviderInfo.setAuthorizationUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".authorizationUrl"));
        openIdProviderInfo.setJwkSetUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".jwkSetUrl"));
        openIdProviderInfo.setLogoutUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".logoutUrl"));
        openIdProviderInfo.setTokenUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".tokenUrl"));
        openIdProviderInfo.setUserInfoUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".userInfoUrl"));
        openIdProviderInfo.setRedirectUrlTemplate("{baseUrl}/oauth2/code/{registrationId}");
        openIdProviderInfo.setUserNameAttributeName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".userNameAttributeName"));
        String scopes = environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".scopes");
        List<String> scopesList = new ArrayList<>();
        if (scopes != null) {
            Collections.addAll(scopesList, scopes.split(","));
        }
        openIdProviderInfo.setScopes(scopesList);
        return openIdProviderInfo;
    }

    @Bean(name = "openIdProviderRuntimeConfig")
    public OpenIdProviderRuntimeConfig openIdProviderRuntimeConfig() {
        List<OpenIdProviderInfo> openIdProvidersInfoList = new ArrayList<>();
        openIdProvidersInfoList.add(openIdProviderInfo());
        OpenIdProviderRuntimeConfig openIdProviderRuntimeConfig = new OpenIdProviderRuntimeConfig();
        openIdProviderRuntimeConfig.setOpenIdProviderInfoList(openIdProvidersInfoList);
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

    @Bean(name = "openIdUserService")
    public OAuth2UserService<? extends OAuth2UserRequest, ? extends OAuth2User> openIdUserService() {
        return new OpenIdUserService();
    }

    @Bean("openIdAuthoritiesProvider")
    @Conditional(OpenIdRoleMappingCondition.class)
    public AuthoritiesProvider userAuthoritiesProvider() {
        IdentityProviderUserAuthoritiesProvider identityProviderUserAuthoritiesProvider = new IdentityProviderUserAuthoritiesProvider();
        identityProviderUserAuthoritiesProvider.setRoleAttributeName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".roleAttributeName"));
        return identityProviderUserAuthoritiesProvider;
    }

    @Bean("openIdAuthoritiesProvider")
    @Conditional(OpenIdDatabaseRoleMappingCondition.class)
    public AuthoritiesProvider databaseUserAuthoritiesProvider(ApplicationContext applicationContext) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();

        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations)
            applicationContext.getBean(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".database.modelName") + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager)
            applicationContext.getBean(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".database.modelName") + "TransactionManager"));
        defaultAuthoritiesProvider.setHql(Objects.equals(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".database.queryType"), "HQL"));
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdActiveProvider + ".database.rolesByUsernameQuery"));
        return defaultAuthoritiesProvider;
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler securityContextLogoutHandler,
                                     LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
