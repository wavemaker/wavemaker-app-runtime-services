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

package com.wavemaker.runtime.security.provider.openId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.NimbusAuthorizationCodeTokenResponseClient;
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
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.commons.auth.openId.OpenIdProviderInfo;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledBaseConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMOpenIdAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMOpenIdLogoutSuccessHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional({SecurityEnabledCondition.class, OpenIdProviderCondition.class})
public class OpenIdSecurityProviderConfiguration implements WMSecurityConfiguration {

    private static final String SECURITY_PROVIDERS_OPEN_ID = "security.providers.openId.";
    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;

    @Value("${security.providers.openId.activeProviders}")
    private String openidActiveRoleProvider;

    @Override
    public void addInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        authorizeRequestsCustomizer
            .requestMatchers(AntPathRequestMatcher.antMatcher("/auth/oauth2/")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/oauth2/code")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/services/oauth2/**/callback/")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/services/security/ssologin")).authenticated();
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterBefore(openIDAuthorizationRequestRedirectFilter(), OAuth2LoginAuthenticationFilter.class);
        http.addFilterAt(openIdLoginAuthenticationFilter(securityEnabledBaseConfiguration.authenticationManager()),
            OAuth2LoginAuthenticationFilter.class);
    }

    @Bean(name = "openIdEntryPoint")
    public AuthenticationEntryPoint openIdAuthenticationEntryPoint() {
        OpenIdAuthenticationEntryPoint openIdAuthenticationEntryPoint = new OpenIdAuthenticationEntryPoint();
        openIdAuthenticationEntryPoint.setProviderId(environment.getProperty("security.providers.openId.activeProviders"));
        return openIdAuthenticationEntryPoint;
    }

    @Bean(name = "oauth2LoginAuthenticationFilter")
    public Filter openIdLoginAuthenticationFilter(AuthenticationManager authenticationManager) {
        OpenIdLoginAuthenticationFilter authenticationFilter = new OpenIdLoginAuthenticationFilter(inMemoryRegistrationRepository(),
            inMemoryOAuth2AuthorizedClientService(), "/oauth2/code/*");
        authenticationFilter.setAuthorizationRequestRepository(
            (AuthorizationRequestRepository<OAuth2AuthorizationRequest>) openIDAuthorizationRequestRepository());
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setAuthenticationSuccessHandler(securityEnabledBaseConfiguration.successHandler());
        authenticationFilter.setSessionAuthenticationStrategy(securityEnabledBaseConfiguration.compositeSessionAuthenticationStrategy());
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
        InMemoryRegistrationRepository inMemoryRegistrationRepository = new InMemoryRegistrationRepository();
        return inMemoryRegistrationRepository;
    }

    @Bean(name = "openIdProviderInfo")
    public OpenIdProviderInfo openIdProviderInfo() {
        OpenIdProviderInfo openIdProviderInfo = new OpenIdProviderInfo();
        String openIdProvider = environment.getProperty("security.providers.openId.activeProviders");
        openIdProviderInfo.setProviderId(openIdProvider);
        openIdProviderInfo.setClientId(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".clientId"));
        openIdProviderInfo.setClientSecret(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".clientSecret"));
        openIdProviderInfo.setAuthorizationUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".authorizationUrl"));
        openIdProviderInfo.setJwkSetUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".jwkSetUrl"));
        openIdProviderInfo.setLogoutUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".logoutUrl"));
        openIdProviderInfo.setTokenUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".tokenUrl"));
        openIdProviderInfo.setUserInfoUrl(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".userInfoUrl"));
        openIdProviderInfo.setRedirectUrlTemplate("{baseUrl}/oauth2/code/{registrationId}");
        openIdProviderInfo.setUserNameAttributeName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".userNameAttributeName"));
        String scopes = environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openIdProvider + ".scopes");
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
    public WMAuthenticationSuccessHandler wmOpenIdAuthenticationSuccessHandler() {
        return new WMOpenIdAuthenticationSuccessHandler();
    }

    @Bean(name = "oAuth2AccessTokenResponseClient")
    public OAuth2AccessTokenResponseClient<? extends AbstractOAuth2AuthorizationGrantRequest> nimbusAuthorizationCodeTokenResponseClient() {
        return new NimbusAuthorizationCodeTokenResponseClient();
    }

    @Bean(name = "openIdAuthenticationProvider")
    public AuthenticationProvider oidcAuthorizationCodeAuthenticationProvider(OAuth2AccessTokenResponseClient<? extends AbstractOAuth2AuthorizationGrantRequest>
                                                                                  nimbusAuthorizationCodeTokenResponseClient,
                                                                              OAuth2UserService<? extends OAuth2UserRequest, ? extends OAuth2User>
                                                                                  openIdUserService) {
        return new OidcAuthorizationCodeAuthenticationProvider(
            (OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>) nimbusAuthorizationCodeTokenResponseClient,
            (OAuth2UserService<OidcUserRequest, OidcUser>) openIdUserService);
    }

    @Bean(name = "openIdUserService")
    public OAuth2UserService<? extends OAuth2UserRequest, ? extends OAuth2User> openIdUserService() {
        return new OpenIdUserService();
    }

    @Bean("userAuthoritiesProvider")
    @Conditional(OpenIdRoleMappingCondition.class)
    public AuthoritiesProvider userAuthoritiesProvider() {
        IdentityProviderUserAuthoritiesProvider identityProviderUserAuthoritiesProvider = new IdentityProviderUserAuthoritiesProvider();
        identityProviderUserAuthoritiesProvider.setRoleAttributeName(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openidActiveRoleProvider + ".roleAttributeName"));
        return identityProviderUserAuthoritiesProvider;
    }

    @Bean("userAuthoritiesProvider")
    @Conditional(OpenIdDatabaseRoleMappingCondition.class)
    public AuthoritiesProvider databaseUserAuthoritiesProvider(ApplicationContext applicationContext) {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();

        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations)
            applicationContext.getBean(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openidActiveRoleProvider + ".database.modelName") + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager)
            applicationContext.getBean(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openidActiveRoleProvider + ".database.modelName") + "TransactionManager"));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        defaultAuthoritiesProvider.setHql(Boolean.TRUE.equals(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openidActiveRoleProvider + ".database.isHQL", Boolean.class)));
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty(SECURITY_PROVIDERS_OPEN_ID + openidActiveRoleProvider + ".database.rolesByUsernameQuery"));
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
