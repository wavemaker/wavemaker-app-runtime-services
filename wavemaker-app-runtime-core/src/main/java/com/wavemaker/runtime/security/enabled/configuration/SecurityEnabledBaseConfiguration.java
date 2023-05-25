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

package com.wavemaker.runtime.security.enabled.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.SessionRepositoryFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.model.security.CSRFConfig;
import com.wavemaker.commons.model.security.LoginConfig;
import com.wavemaker.commons.model.security.LoginType;
import com.wavemaker.commons.model.security.RememberMeConfig;
import com.wavemaker.commons.model.security.RoleConfig;
import com.wavemaker.commons.model.security.RolesConfig;
import com.wavemaker.commons.model.security.SessionTimeoutConfig;
import com.wavemaker.commons.model.security.TokenAuthConfig;
import com.wavemaker.runtime.security.WMAppAccessDeniedHandler;
import com.wavemaker.runtime.security.WMApplicationAuthenticationFailureHandler;
import com.wavemaker.runtime.security.WMAuthenticationEntryPoint;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.csrf.CsrfSecurityRequestMatcher;
import com.wavemaker.runtime.security.csrf.WMCsrfFilter;
import com.wavemaker.runtime.security.csrf.WMCsrfLogoutHandler;
import com.wavemaker.runtime.security.csrf.WMCsrfTokenRepository;
import com.wavemaker.runtime.security.csrf.WMHttpSessionCsrfTokenRepository;
import com.wavemaker.runtime.security.enabled.configuration.RequestMatchers.BasicAuthRequestMatcher;
import com.wavemaker.runtime.security.enabled.configuration.RequestMatchers.DefaultRequestMatcher;
import com.wavemaker.runtime.security.enabled.configuration.models.SecurityInterceptUrlEntry;
import com.wavemaker.runtime.security.entrypoint.WMCompositeAuthenticationEntryPoint;
import com.wavemaker.runtime.security.filter.WMTokenBasedPreAuthenticatedProcessingFilter;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessRedirectionHandler;
import com.wavemaker.runtime.security.handler.WMCsrfTokenRepositorySuccessHandler;
import com.wavemaker.runtime.security.handler.WMCsrfTokenResponseWriterAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMSecurityContextRepositorySuccessHandler;
import com.wavemaker.runtime.security.model.Role;
import com.wavemaker.runtime.security.openId.OpenIdConfiguration;
import com.wavemaker.runtime.security.rememberme.config.RememberMeConfiguration;
import com.wavemaker.runtime.security.token.WMTokenBasedAuthenticationService;
import com.wavemaker.runtime.security.token.repository.WMTokenRepository;
import com.wavemaker.runtime.webprocess.filter.LoginProcessFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Import(FilterSecurityInterceptorBeanPostProcessor.class)
@Conditional(SecurityEnabledCondition.class)
public class SecurityEnabledBaseConfiguration {
    @Autowired(required = false)
    private OpenIdConfiguration openIdConfiguration;

    @Autowired(required = false)
    private RememberMeConfiguration rememberMeConfiguration;

    @Autowired
    private List<AuthenticationProvider> authenticationProvidersList;

    @Autowired
    private List<WMSecurityConfiguration> wmSecurityConfiguration;

    @Autowired
    FindByIndexNameSessionRepository sessionRepository;

    @Autowired
    SpringSessionBackedSessionRegistry sessionRegistry;

    @Value("${security.providers.activeProviders}")
    private String activeProvider;

    @Value("${security.general.xsrf.enabled}")
    private boolean enforceCsrfSecurity;

    @Value("${security.general.xsrf.headerName}")
    private String csrfHeaderName;

    @Value("${security.general.xsrf.cookieName}")
    private String csrfCookieName;

    @Value("${security.general.rememberMe.enabled}")
    private boolean isRememberEnabled;

    @Value("${security.general.rememberMe.timeOut}")
    private long rememberTimeOut;

    @Value("${security.general.login.type}")
    private LoginType loginType;

    @Value("${security.general.login.pageName}")
    private String pageName;

    @Value("${security.general.login.sessionTimeoutType}")
    private LoginType sessioLoginType;

    @Value("${security.general.login.sessionTimeoutPageName}")
    private String sessionPageName;

    @Value("${security.general.session.timeout}")
    private int timeOutValue;

    @Value("${security.general.tokenService.tokenValiditySeconds}")
    private int tokenValiditySeconds;

    @Value("${security.general.tokenService.enabled}")
    private boolean isEnabled;

    @Value("${security.general.tokenService.parameter}")
    private String parameter;

    @Bean
    public DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler() {
        return new DefaultWebSecurityExpressionHandler();
    }

    @Bean(name = "authenticationManager")
    public AuthenticationManager authenticationManager(HttpSecurity http) {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationProvidersList.forEach(auth::authenticationProvider);
        /*if (activeProvider.equals("AD") || activeProvider.equals("OPENID")) {
            return auth.getOrBuild();
        }*/
        return auth.getOrBuild();
    }

    @Bean
    public CookieSerializer defaultCookieSerializer() {
        return new DefaultCookieSerializer();
    }

    @Bean
    public CookieHttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver cookieHttpSessionIdResolver = new CookieHttpSessionIdResolver();
        cookieHttpSessionIdResolver.setCookieSerializer(defaultCookieSerializer());
        return cookieHttpSessionIdResolver;
    }

    @Bean(name = "sessionRepositoryFilter")
    public SessionRepositoryFilter sessionRepositoryFilter() {
        SessionRepositoryFilter sessionRepositoryFilter = new SessionRepositoryFilter(sessionRepository);
        sessionRepositoryFilter.setHttpSessionIdResolver(httpSessionIdResolver());
        return sessionRepositoryFilter;
    }

    @Bean(name = "wmcsrfFilter")
    public WMCsrfFilter wmCsrfFilter() {
        return new WMCsrfFilter(csrfTokenRepository(), csrfSecurityRequestMatcher());
    }

    @Bean
    public WMCompositeAuthenticationEntryPoint appAuthenticationEntryPoint() {
        return new WMCompositeAuthenticationEntryPoint();
    }

    @Bean
    public SessionFixationProtectionStrategy sessionFixationProtectionStrategy() {
        return new SessionFixationProtectionStrategy();
    }

    @Bean
    public CsrfAuthenticationStrategy csrfAuthenticationStrategy() {
        return new CsrfAuthenticationStrategy(csrfTokenRepository());
    }

    @Bean(name = "compositeSessionAuthenticationStrategy")
    public CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy() {
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(-1);
        concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(false);
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>(Arrays.asList(concurrentSessionControlAuthenticationStrategy, registerSessionAuthenticationStrategy, sessionFixationProtectionStrategy(), csrfAuthenticationStrategy()));
        return new CompositeSessionAuthenticationStrategy(delegateStrategies);
    }

//    @Bean(name = "compositeSessionAuthenticationStrategy")
//    public CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy() {
//        List<SessionAuthenticationStrategy> delegateStrategiesForNestedBean = new ArrayList<>(Arrays.asList(sessionFixationProtectionStrategy(), csrfAuthenticationStrategy()));
//        return new CompositeSessionAuthenticationStrategy(delegateStrategiesForNestedBean);
//    }

    @Bean
    public WMAppAccessDeniedHandler wmAppAccessDeniedHandler() {
        return new WMAppAccessDeniedHandler();
    }

    @Bean
    public WMCsrfTokenRepository csrfTokenRepository() {
        return new WMCsrfTokenRepository(wmHttpSessionCsrfTokenRepository());
    }

    @Bean
    public CSRFConfig csrfConfig() {
        CSRFConfig config = new CSRFConfig();
        config.setEnforceCsrfSecurity(enforceCsrfSecurity);
        config.setHeaderName(csrfHeaderName);
        config.setCookieName(csrfCookieName);
        return config;
    }

    @Bean
    public WMHttpSessionCsrfTokenRepository wmHttpSessionCsrfTokenRepository() {
        WMHttpSessionCsrfTokenRepository wmHttpSessionCsrfTokenRepository = new WMHttpSessionCsrfTokenRepository();
        wmHttpSessionCsrfTokenRepository.setCsrfConfig(csrfConfig());
        return wmHttpSessionCsrfTokenRepository;
    }

    @Bean
    public CsrfSecurityRequestMatcher csrfSecurityRequestMatcher() {
        CsrfSecurityRequestMatcher requestMatcher = new CsrfSecurityRequestMatcher();
        requestMatcher.setCsrfConfig(csrfConfig());
        return requestMatcher;
    }

//    @Bean(name = "logoutFilter")
//    public LogoutFilter logoutFilter() {
//        LogoutFilter logoutFilter = createLogoutFilter();
//        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
//        return logoutFilter;
//    }

    @Bean
    public WMCsrfLogoutHandler wmCsrfLogoutHandler() {
        return new WMCsrfLogoutHandler(csrfLogoutHandler());
    }

    @Bean
    public CsrfLogoutHandler csrfLogoutHandler() {
        return new CsrfLogoutHandler(csrfTokenRepository());
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean
    public DefaultRedirectStrategy redirectStrategyBean() {
        return new DefaultRedirectStrategy();
    }

    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }

    @Bean
    public NullRequestCache nullRequestCache() {
        return new NullRequestCache();
    }

    @Bean(name = "wmTokenBasedPreAuthenticatedProcessingFilter")
    public WMTokenBasedPreAuthenticatedProcessingFilter wmTokenBasedPreAuthenticatedProcessingFilter(HttpSecurity http) {
        return new WMTokenBasedPreAuthenticatedProcessingFilter(authenticationManager(http), wmTokenBasedAuthenticationService());
    }

    @Bean
    public WMTokenRepository tokenRepository() {
        return new WMTokenRepository();
    }

    @Bean
    public WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService() {
        return new WMTokenBasedAuthenticationService();
    }

    @Bean
    public WMApplicationAuthenticationSuccessHandler successHandler() {
        List<AuthenticationSuccessHandler> defaultSuccessHandlerList = new ArrayList<>();
        defaultSuccessHandlerList.add(wmSecurityContextRepositorySuccessHandler());
        defaultSuccessHandlerList.add(wmCsrfTokenRepositorySuccessHandler());
        defaultSuccessHandlerList.add(wmCsrfTokenResponseWriterAuthenticationSuccessHandler());
        WMApplicationAuthenticationSuccessHandler wmApplicationAuthenticationSuccessHandler = new WMApplicationAuthenticationSuccessHandler();
        wmApplicationAuthenticationSuccessHandler.setDefaultSuccessHandlerList(defaultSuccessHandlerList);
        wmApplicationAuthenticationSuccessHandler.setAuthenticationSuccessRedirectionHandler(wmAuthenticationSuccessRedirectionHandler());
        return wmApplicationAuthenticationSuccessHandler;
    }

    @Bean
    public WMAuthenticationSuccessRedirectionHandler wmAuthenticationSuccessRedirectionHandler() {
        return new WMAuthenticationSuccessRedirectionHandler();
    }

    @Bean
    public WMCsrfTokenRepositorySuccessHandler wmCsrfTokenRepositorySuccessHandler() {
        return new WMCsrfTokenRepositorySuccessHandler(csrfTokenRepository());
    }

    @Bean
    public WMCsrfTokenResponseWriterAuthenticationSuccessHandler wmCsrfTokenResponseWriterAuthenticationSuccessHandler() {
        return new WMCsrfTokenResponseWriterAuthenticationSuccessHandler(csrfTokenRepository());
    }

    @Bean
    public HttpSessionSecurityContextRepository securityContextRepository() {
        HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();
        httpSessionSecurityContextRepository.setDisableUrlRewriting(true);
        return httpSessionSecurityContextRepository;
    }

    @Bean
    public HttpSessionSecurityContextRepository securityContextRepositoryForBackend() {
        HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();
        httpSessionSecurityContextRepository.setDisableUrlRewriting(true);
        httpSessionSecurityContextRepository.setAllowSessionCreation(false);
        return httpSessionSecurityContextRepository;
    }

    @Bean
    public WMSecurityContextRepositorySuccessHandler wmSecurityContextRepositorySuccessHandler() {
        return new WMSecurityContextRepositorySuccessHandler(securityContextRepository());
    }

    @Bean
    public WMApplicationAuthenticationFailureHandler failureHandler() {
        return new WMApplicationAuthenticationFailureHandler();
    }

    @Bean
    public RememberMeConfig rememberMeConfig() {
        RememberMeConfig rememberMeConfig = new RememberMeConfig();
        rememberMeConfig.setEnabled(isRememberEnabled);
        rememberMeConfig.setTokenValiditySeconds(rememberTimeOut);
        return rememberMeConfig;
    }

    @Bean
    public LoginConfig loginConfig() {
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setType(loginType);
        loginConfig.setPageName(pageName);
        loginConfig.setSessionTimeout(sessionTimeoutConfig());
        return loginConfig;
    }

    @Bean
    public SessionTimeoutConfig sessionTimeoutConfig() {
        SessionTimeoutConfig sessionTimeoutConfig = new SessionTimeoutConfig();
        sessionTimeoutConfig.setType(sessioLoginType);
        sessionTimeoutConfig.setPageName(sessionPageName);
        sessionTimeoutConfig.setTimeoutValue(timeOutValue);
        return sessionTimeoutConfig;
    }

    @Bean
    public TokenAuthConfig tokenAuthConfig() {
        TokenAuthConfig tokenAuthConfig = new TokenAuthConfig();
        tokenAuthConfig.setEnabled(isEnabled);
        tokenAuthConfig.setParameter(parameter);
        tokenAuthConfig.setTokenValiditySeconds(tokenValiditySeconds);
        return tokenAuthConfig;
    }

    @Bean(name = "loginWebProcessFilter")
    public LoginProcessFilter loginWebProcessFilter() {
        return new LoginProcessFilter();
    }

    @Bean
    public WMAuthenticationEntryPoint wmSecAuthEntryPoint() {
        return new WMAuthenticationEntryPoint("/index.html");
    }

    @Bean(name = "rolesConfig")
    public RolesConfig rolesConfig() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("conf/roles.json");
            List<Role> roles = jsonTOObject(classPathResource.getFile(), Role.class);
            return createRoleConfig(roles);
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    public RolesConfig createRoleConfig(List<Role> roles) {
        RolesConfig rolesConfiguration = new RolesConfig();
        Map<String, RoleConfig> roleMap = new HashMap<>();
        for (Role roleConfig : roles) {
            roleMap.put(roleConfig.getName(), roleConfig.getRoleConfig());
        }
        rolesConfiguration.setRoleMap(roleMap);
        return rolesConfiguration;
    }

    public <T> List<T> jsonTOObject(File file, Class<T> tclass) {
        try {
            TypeFactory t = TypeFactory.defaultInstance();
            CollectionType collectionType = t.constructCollectionType(ArrayList.class, tclass);
            return JSONUtils.toObject(file, collectionType);
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    @Bean
    public WebSecurityCustomizer ignoreAntMatchers() {
        return (web -> {
            try {
                setDefaultAntMatchers(web.ignoring());
            } catch (Exception e) {
                throw new WMRuntimeException(e);
            }
        });
    }

    @Bean
    public SecurityFilterChain filterChainForWeb(HttpSecurity http, LogoutFilter logoutFilter) {
        try {
            http
                .csrf().disable()
                .headers().disable()
                .securityMatcher(new DefaultRequestMatcher())
                .sessionManagement().sessionFixation().migrateSession()
                .maximumSessions(-1).sessionRegistry(sessionRegistry)
                .and()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()
                .requestCache()
                .requestCache(nullRequestCache())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(appAuthenticationEntryPoint())
                .and()
                .securityContext().securityContextRepository(securityContextRepository())
                .and()
                .authenticationManager(authenticationManager(http));
            http.authorizeRequests(a ->
                    executeUrls(a))
                .logout().disable()
                .addFilterAt(sessionRepositoryFilter(), ChannelProcessingFilter.class)
                .addFilterAt(wmCsrfFilter(), CsrfFilter.class)
                .addFilterBefore(wmTokenBasedPreAuthenticatedProcessingFilter(http), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAt(logoutFilter, LogoutFilter.class)
                .addFilterAfter(loginWebProcessFilter(), SecurityContextPersistenceFilter.class);
            wmSecurityConfiguration.forEach(securityConfiguration ->
                securityConfiguration.executeFilters(http));
            return http.build();
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    @Bean
    public SecurityFilterChain filterChainForApi(HttpSecurity http) {
        try {
            http
                .csrf().disable()
                .headers().disable()
                .securityMatcher(new BasicAuthRequestMatcher())
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .requestCache()
                .requestCache(nullRequestCache())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(appAuthenticationEntryPoint())
                .and()
                .securityContext().securityContextRepository(securityContextRepositoryForBackend())
                .and()
                .authenticationManager(authenticationManager(http));
            http.authorizeRequests(a ->
                    executeUrls(a))
                .logout().disable()
                .addFilterAt(wmCsrfFilter(), CsrfFilter.class);
            wmSecurityConfiguration.forEach(securityConfiguration -> {
                securityConfiguration.executeFilters(http);
            });
            return http.build();
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    public void setDefaultAntMatchers(WebSecurity.IgnoredRequestConfigurer web) {
        try {
            List<SecurityInterceptUrlEntry> defaultAntMatchers = JSONUtils.toObject(this.getClass().getClassLoader().getResourceAsStream("conf/default-antmatchers.json"), new TypeReference<>() {
            });
            for (SecurityInterceptUrlEntry antMatchers : defaultAntMatchers) {
                web.antMatchers(antMatchers.getUrlPattern());
            }
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    public void setAntMatchers(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer,
                               SecurityInterceptUrlEntry securityInterceptUrlEntry) {
        switch (securityInterceptUrlEntry.getPermission()) {
            case "Authenticated":
                authorizeRequestsCustomizer.antMatchers(securityInterceptUrlEntry.getHttpMethod(), securityInterceptUrlEntry.getUrlPattern()).authenticated();
                break;
            case "Role":
                authorizeRequestsCustomizer.antMatchers(securityInterceptUrlEntry.getHttpMethod(), securityInterceptUrlEntry.getUrlPattern()).hasAnyRole(securityInterceptUrlEntry.getRoles());
                break;
            default:
                authorizeRequestsCustomizer.antMatchers(securityInterceptUrlEntry.getHttpMethod(), securityInterceptUrlEntry.getUrlPattern()).permitAll();
                break;
        }
    }

/*    public LogoutFilter createLogoutFilter() {
        LogoutFilter logoutFilter = null;
        switch (activeProvider) {
            case "DEMO":
            case "DATABASE":
            case "LDAP":
                logoutFilter = new LogoutFilter(logoutSuccessHandler(), securityContextLogoutHandler(), wmCsrfLogoutHandler(), rememberMeConfiguration.rememberMeServices());
                break;
            case "AD":
            case "CUSTOM":
                logoutFilter = new LogoutFilter(logoutSuccessHandler(), securityContextLogoutHandler(), wmCsrfLogoutHandler());
                break;
            case "OPENID":
                logoutFilter = new LogoutFilter(openIdConfiguration.getWMOpenIdLogoutSuccessHandler(redirectStrategyBean()), securityContextLogoutHandler(), wmCsrfLogoutHandler());
                break;
        }
        return logoutFilter;
    }*/

    public void executeInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        authorizeRequestsCustomizer.antMatchers("/index.html").permitAll()
            .antMatchers("/j_spring_security_logout").permitAll()
            .antMatchers("/services/application/i18n/**").permitAll()
            .antMatchers("/services/prefabs/**/servicedefs").permitAll()
            .antMatchers("/services/security/**").permitAll()
            .antMatchers("/services/servicedefs").permitAll()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/").authenticated()
            .antMatchers("/**").authenticated();
    }

    public void executeUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        try {
            ClassPathResource classPathResourceForCustomInterceptUrls = new ClassPathResource("conf/intercept-urls.json");
            List<SecurityInterceptUrlEntry> customInterceptUrls = jsonTOObject(classPathResourceForCustomInterceptUrls.getFile(), SecurityInterceptUrlEntry.class);
            wmSecurityConfiguration.forEach(securityConfiguration -> {
                securityConfiguration.executeInterceptUrls(authorizeRequestsCustomizer);
            });
            for (SecurityInterceptUrlEntry customInterceptUrl : customInterceptUrls) {
                if (!customInterceptUrl.getUrlPattern().equals("/**")) {
                    setAntMatchers(authorizeRequestsCustomizer, customInterceptUrl);
                }
            }
            executeInterceptUrls(authorizeRequestsCustomizer);
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }
}