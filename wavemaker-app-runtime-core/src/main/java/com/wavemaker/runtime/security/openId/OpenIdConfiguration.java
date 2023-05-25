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

package com.wavemaker.runtime.security.openId;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.NimbusAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.transaction.PlatformTransactionManager;

import com.wavemaker.commons.auth.openId.OpenIdProviderInfo;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMOpenIdAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMOpenIdLogoutSuccessHandler;
import com.wavemaker.runtime.security.provider.database.authorities.DefaultAuthoritiesProviderImpl;

@Configuration
@Conditional(OpenIdProviderCondition.class)
public class OpenIdConfiguration {
    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy;

    @Bean(name = "openIdEntryPoint")//6
    public OpenIdAuthenticationEntryPoint getOpenIdAuthenticationEntryPoint() {
        OpenIdAuthenticationEntryPoint openIdAuthenticationEntryPoint = new OpenIdAuthenticationEntryPoint();
        openIdAuthenticationEntryPoint.setProviderId(environment.getProperty("security.providers.openId.activeProviders"));
        return openIdAuthenticationEntryPoint;
    }

    @Bean(name = "oauth2LoginAuthenticationFilter")//5
    public OpenIdLoginAuthenticationFilter getOpenIdLoginAuthenticationFilter(AuthenticationManager authenticationManager,
                                                                              WMApplicationAuthenticationSuccessHandler successHandler) {
        OpenIdLoginAuthenticationFilter authenticationFilter = new OpenIdLoginAuthenticationFilter(getInMemoryRegistrationRepository(),
            getInMemoryOAuth2AuthorizedClientService(), "/oauth2/code/*");
        authenticationFilter.setAuthorizationRequestRepository(getOpenIDAuthorizationRequestRepository());
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setAuthenticationSuccessHandler(successHandler);
        authenticationFilter.setSessionAuthenticationStrategy(compositeSessionAuthenticationStrategy);
        return authenticationFilter;
    }

    @Bean(name = "logoutSuccessHandler")
    public WMOpenIdLogoutSuccessHandler getWMOpenIdLogoutSuccessHandler(DefaultRedirectStrategy redirectStrategy) {
        WMOpenIdLogoutSuccessHandler wmOpenIdLogoutSuccessHandler = new WMOpenIdLogoutSuccessHandler();
        wmOpenIdLogoutSuccessHandler.setDefaultTargetUrl("/");
        wmOpenIdLogoutSuccessHandler.setRedirectStrategy(redirectStrategy);
        return wmOpenIdLogoutSuccessHandler;
    }

    @Bean(name = "inMemoryOAuth2AuthorizedClientService")
    public InMemoryOAuth2AuthorizedClientService getInMemoryOAuth2AuthorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(getInMemoryRegistrationRepository());
    }

    @Bean(name = "authorizationRequestRedirectFilter") //2
    public OpenIDAuthorizationRequestRedirectFilter getOpenIDAuthorizationRequestRedirectFilter() {
        OpenIDAuthorizationRequestRedirectFilter openIDAuthorizationRequestRedirectFilter = new OpenIDAuthorizationRequestRedirectFilter(
            getInMemoryRegistrationRepository(), "/auth/oauth2");
        openIDAuthorizationRequestRedirectFilter.setAuthorizationRequestRepository(getOpenIDAuthorizationRequestRepository());
        return openIDAuthorizationRequestRedirectFilter;
    }

    @Bean(name = "openIDAuthorizationRequestRepository")//4
    public HttpSessionOAuth2AuthorizationRequestRepository getOpenIDAuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean(name = "inMemoryRegistrationRepository")
    public InMemoryRegistrationRepository getInMemoryRegistrationRepository() {
        InMemoryRegistrationRepository inMemoryRegistrationRepository = new InMemoryRegistrationRepository();
        return inMemoryRegistrationRepository;
    }

    @Bean(name = "openIdProviderInfo")
    public OpenIdProviderInfo getOpenIdProviderInfo() {
        OpenIdProviderInfo openIdProviderInfo = new OpenIdProviderInfo();
        String provider = environment.getProperty("security.providers.openId.activeProviders");
        String openIdProvider = "security.providers.openId.";
        openIdProviderInfo.setProviderId(provider);
        openIdProviderInfo.setClientId(environment.getProperty(openIdProvider + provider + ".clientId"));
        openIdProviderInfo.setClientSecret(environment.getProperty(openIdProvider + provider + ".clientSecret"));
        openIdProviderInfo.setAuthorizationUrl(environment.getProperty(openIdProvider + provider + ".authorizationUrl"));
        openIdProviderInfo.setJwkSetUrl(environment.getProperty(openIdProvider + provider + ".jwkSetUrl"));
        openIdProviderInfo.setLogoutUrl(environment.getProperty(openIdProvider + provider + ".logoutUrl"));
        openIdProviderInfo.setTokenUrl(environment.getProperty(openIdProvider + provider + ".tokenUrl"));
        openIdProviderInfo.setUserInfoUrl(environment.getProperty(openIdProvider + provider + ".userInfoUrl"));
        openIdProviderInfo.setRedirectUrlTemplate("{baseUrl}/oauth2/code/{registrationId}");
        openIdProviderInfo.setUserNameAttributeName(environment.getProperty(openIdProvider + provider + ".userNameAttributeName"));
        String scopes = environment.getProperty(openIdProvider + provider + ".scopes");
        List<String> scopesList = new ArrayList<>();
        for (String scopeItem : scopes.split(",")) {
            scopesList.add(scopeItem);
        }
        openIdProviderInfo.setScopes(scopesList);
        return openIdProviderInfo;
    }

    @Bean(name = "openIdProviderRuntimeConfig") //3
    public OpenIdProviderRuntimeConfig getOpenIdProviderRuntimeConfig() {
        List<OpenIdProviderInfo> openIdProvidersInfoList = new ArrayList<>();
        openIdProvidersInfoList.add(getOpenIdProviderInfo());
        OpenIdProviderRuntimeConfig openIdProviderRuntimeConfig = new OpenIdProviderRuntimeConfig();
        openIdProviderRuntimeConfig.setOpenIdProviderInfoList(openIdProvidersInfoList);
        return openIdProviderRuntimeConfig;
    }

    @Bean(name = "openIdAuthenticationSuccessHandler")//7
    public WMOpenIdAuthenticationSuccessHandler getWMOpenIdAuthenticationSuccessHandler() {
        return new WMOpenIdAuthenticationSuccessHandler();
    }

    @Bean(name = "oAuth2AccessTokenResponseClient")
    public NimbusAuthorizationCodeTokenResponseClient getNimbusAuthorizationCodeTokenResponseClient() {
        return new NimbusAuthorizationCodeTokenResponseClient();
    }

    @Bean(name = "openIdAuthenticationProvider")
    public OidcAuthorizationCodeAuthenticationProvider getOidcAuthorizationCodeAuthenticationProvider() {
        return new OidcAuthorizationCodeAuthenticationProvider(getNimbusAuthorizationCodeTokenResponseClient(), getOpenIdUserService());
    }

    @Bean(name = "openIdUserService")
    public OpenIdUserService getOpenIdUserService() {
        return new OpenIdUserService();
    }

    public void executeAntMatchers(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        authorizeRequestsCustomizer.antMatchers("/auth/oauth2/").permitAll()
            .antMatchers("/oauth2/code").permitAll()
            .antMatchers("/services/oauth2/**/callback/").permitAll()
            .antMatchers("/services/security/ssologin").authenticated();
    }

    public void executeFilters(HttpSecurity http, AuthenticationManager authenticationManager, WMApplicationAuthenticationSuccessHandler successHandler) {
        http.addFilterBefore(getOpenIDAuthorizationRequestRedirectFilter(), OAuth2LoginAuthenticationFilter.class);
        http.addFilterAt(getOpenIdLoginAuthenticationFilter(authenticationManager, successHandler),
            OAuth2LoginAuthenticationFilter.class);
    }

    @Bean("userAuthoritiesProvider")
    @Conditional(OpenIdRoleMappingCondition.class)
    public IdentityProviderUserAuthoritiesProvider getUserAuthoritiesProvider() {
        IdentityProviderUserAuthoritiesProvider identityProviderUserAuthoritiesProvider = new IdentityProviderUserAuthoritiesProvider();
        identityProviderUserAuthoritiesProvider.setRoleAttributeName(environment.getProperty("security.providers.openId.roleAttributeName"));
        return identityProviderUserAuthoritiesProvider;
    }

    @Bean
    @Conditional(OpenIdDatabaseRoleMappingCondition.class)
    public DefaultAuthoritiesProviderImpl getDatabaseUserAuthoritiesProvider() {
        DefaultAuthoritiesProviderImpl defaultAuthoritiesProvider = new DefaultAuthoritiesProviderImpl();
        defaultAuthoritiesProvider.setHibernateTemplate((HibernateOperations)
            applicationContext.getBean(environment.getProperty("security.providers.openId.database.modelName") + "Template"));
        defaultAuthoritiesProvider.setTransactionManager((PlatformTransactionManager)
            applicationContext.getBean(environment.getProperty("security.providers.openId.database.modelName") + "TransactionManager"));
        defaultAuthoritiesProvider.setRolesByQuery(true);
        String queryType = environment.getProperty("security.providers.openId.database.queryType");
        if (queryType.equals("HQL")) {
            defaultAuthoritiesProvider.setHql(true);
        }
        defaultAuthoritiesProvider.setHql(false);
        defaultAuthoritiesProvider.setAuthoritiesByUsernameQuery(environment.getProperty("security.providers.openId.database.roleQuery"));
        return defaultAuthoritiesProvider;
    }
}
