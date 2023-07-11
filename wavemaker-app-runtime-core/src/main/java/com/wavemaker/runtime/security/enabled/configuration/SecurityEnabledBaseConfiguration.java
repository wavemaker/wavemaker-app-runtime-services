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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
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
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;

import com.wavemaker.app.security.models.CSRFConfig;
import com.wavemaker.app.security.models.LoginConfig;
import com.wavemaker.app.security.models.RememberMeConfig;
import com.wavemaker.app.security.models.Role;
import com.wavemaker.app.security.models.RoleConfig;
import com.wavemaker.app.security.models.RoleList;
import com.wavemaker.app.security.models.RolesConfig;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.SecurityInterceptUrlList;
import com.wavemaker.app.security.models.SessionTimeoutConfig;
import com.wavemaker.app.security.models.TokenAuthConfig;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.security.WMAppAccessDeniedHandler;
import com.wavemaker.runtime.security.WMApplicationAuthenticationFailureHandler;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.csrf.CsrfSecurityRequestMatcher;
import com.wavemaker.runtime.security.csrf.WMCsrfFilter;
import com.wavemaker.runtime.security.csrf.WMCsrfLogoutHandler;
import com.wavemaker.runtime.security.csrf.WMCsrfTokenRepository;
import com.wavemaker.runtime.security.csrf.WMHttpSessionCsrfTokenRepository;
import com.wavemaker.runtime.security.enabled.configuration.requestmatcher.StatelessRequestMatcher;
import com.wavemaker.runtime.security.entrypoint.WMCompositeAuthenticationEntryPoint;
import com.wavemaker.runtime.security.filter.WMTokenBasedPreAuthenticatedProcessingFilter;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationRedirectionHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessRedirectionHandler;
import com.wavemaker.runtime.security.handler.WMCsrfTokenRepositorySuccessHandler;
import com.wavemaker.runtime.security.handler.WMCsrfTokenResponseWriterAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMSecurityContextRepositorySuccessHandler;
import com.wavemaker.runtime.security.token.WMTokenBasedAuthenticationService;
import com.wavemaker.runtime.security.token.repository.WMTokenRepository;
import com.wavemaker.runtime.webprocess.filter.LoginProcessFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Conditional(SecurityEnabledCondition.class)
public class SecurityEnabledBaseConfiguration {
    @Autowired
    private List<AuthenticationProvider> authenticationProvidersList;

    @Autowired
    private List<WMSecurityConfiguration> wmSecurityConfiguration;

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    private SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry;

    @Autowired
    private Environment environment;

    @Bean(name = "defaultWebSecurityExpressionHandler")
    public SecurityExpressionHandler<FilterInvocation> defaultWebSecurityExpressionHandler() {
        return new DefaultWebSecurityExpressionHandler();
    }

    @Bean(name = "authenticationManager")
    public AuthenticationManager authenticationManager() {
        try {
            return new ProviderManager(authenticationProvidersList);
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    @Bean(name = "defaultCookieSerializer")
    public CookieSerializer defaultCookieSerializer() {
        return new DefaultCookieSerializer();
    }

    @Bean(name = "httpSessionIdResolver")
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver cookieHttpSessionIdResolver = new CookieHttpSessionIdResolver();
        cookieHttpSessionIdResolver.setCookieSerializer(defaultCookieSerializer());
        return cookieHttpSessionIdResolver;
    }

    @Bean(name = "sessionRepositoryFilter")
    public Filter sessionRepositoryFilter() {
        SessionRepositoryFilter<? extends Session> sessionRepositoryFilter = new SessionRepositoryFilter<>(sessionRepository);
        sessionRepositoryFilter.setHttpSessionIdResolver(httpSessionIdResolver());
        return sessionRepositoryFilter;
    }

    @Bean(name = "wmcsrfFilter")
    public Filter wmCsrfFilter() {
        return new WMCsrfFilter(csrfTokenRepository(), csrfSecurityRequestMatcher());
    }

    //TODO Change the return type to generic to override beans from xml
    @Bean(name = "appAuthenticationEntryPoint")
    public WMCompositeAuthenticationEntryPoint appAuthenticationEntryPoint() {
        return new WMCompositeAuthenticationEntryPoint();
    }

    @Bean(name = "sessionFixationProtectionStrategy")
    public SessionFixationProtectionStrategy sessionFixationProtectionStrategy() {
        return new SessionFixationProtectionStrategy();
    }

    @Bean(name = "csrfAuthenticationStrategy")
    public SessionAuthenticationStrategy csrfAuthenticationStrategy() {
        return new CsrfAuthenticationStrategy(csrfTokenRepository());
    }

    @Bean(name = "compositeSessionAuthenticationStrategy")
    public SessionAuthenticationStrategy compositeSessionAuthenticationStrategy() {
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(-1);
        concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(false);
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>(Arrays.asList(concurrentSessionControlAuthenticationStrategy, registerSessionAuthenticationStrategy, sessionFixationProtectionStrategy(), csrfAuthenticationStrategy()));
        return new CompositeSessionAuthenticationStrategy(delegateStrategies);
    }

    @Bean(name = "wmAppAccessDeniedHandler")
    public AccessDeniedHandler wmAppAccessDeniedHandler() {
        return new WMAppAccessDeniedHandler();
    }

    @Bean(name = "csrfTokenRepository")
    public CsrfTokenRepository csrfTokenRepository() {
        return new WMCsrfTokenRepository(wmHttpSessionCsrfTokenRepository());
    }

    @Bean(name = "csrfConfig")
    public CSRFConfig csrfConfig() {
        return new CSRFConfig();
    }

    @Bean(name = "wmHttpSessionCsrfTokenRepository")
    public CsrfTokenRepository wmHttpSessionCsrfTokenRepository() {
        WMHttpSessionCsrfTokenRepository wmHttpSessionCsrfTokenRepository = new WMHttpSessionCsrfTokenRepository();
        wmHttpSessionCsrfTokenRepository.setCsrfConfig(csrfConfig());
        return wmHttpSessionCsrfTokenRepository;
    }

    @Bean(name = "csrfSecurityRequestMatcher")
    public RequestMatcher csrfSecurityRequestMatcher() {
        CsrfSecurityRequestMatcher requestMatcher = new CsrfSecurityRequestMatcher();
        requestMatcher.setCsrfConfig(csrfConfig());
        return requestMatcher;
    }

    @Bean(name = "wmCsrfLogoutHandler")
    public LogoutHandler wmCsrfLogoutHandler() {
        return new WMCsrfLogoutHandler(csrfLogoutHandler());
    }

    @Bean(name = "csrfLogoutHandler")
    public LogoutHandler csrfLogoutHandler() {
        return new CsrfLogoutHandler(csrfTokenRepository());
    }

    @Bean(name = "logoutSuccessHandler")
    public LogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean());
        return simpleUrlLogoutSuccessHandler;
    }

    @Bean(name = "redirectStrategyBean")
    public RedirectStrategy redirectStrategyBean() {
        return new DefaultRedirectStrategy();
    }

    @Bean(name = "securityContextLogoutHandler")
    public LogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }

    @Bean(name = "nullRequestCache")
    public RequestCache nullRequestCache() {
        return new NullRequestCache();
    }

    @Bean(name = "wmTokenBasedPreAuthenticatedProcessingFilter")
    public Filter wmTokenBasedPreAuthenticatedProcessingFilter() {
        return new WMTokenBasedPreAuthenticatedProcessingFilter(authenticationManager(), wmTokenBasedAuthenticationService());
    }

    //TODO Change the return type to generic to override beans from xml
    @Bean(name = "tokenRepository")
    public WMTokenRepository tokenRepository() {
        return new WMTokenRepository();
    }

    @Bean(name = "wmTokenBasedAuthenticationService")
    public WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService() {
        return new WMTokenBasedAuthenticationService();
    }

    @Bean(name = "successHandler")
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

    //TODO need to check the generic return type
    @Bean(name = "wmAuthenticationSuccessRedirectionHandler")
    public WMAuthenticationRedirectionHandler wmAuthenticationSuccessRedirectionHandler() {
        return new WMAuthenticationSuccessRedirectionHandler();
    }

    @Bean(name = "wmCsrfTokenRepositorySuccessHandler")
    public AuthenticationSuccessHandler wmCsrfTokenRepositorySuccessHandler() {
        return new WMCsrfTokenRepositorySuccessHandler(csrfTokenRepository());
    }

    @Bean(name = "wmCsrfTokenResponseWriterAuthenticationSuccessHandler")
    public AuthenticationSuccessHandler wmCsrfTokenResponseWriterAuthenticationSuccessHandler() {
        return new WMCsrfTokenResponseWriterAuthenticationSuccessHandler(csrfTokenRepository());
    }

    @Bean(name = "securityContextRepository")
    public SecurityContextRepository securityContextRepository() {
        HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();
        httpSessionSecurityContextRepository.setDisableUrlRewriting(true);
        return httpSessionSecurityContextRepository;
    }

    @Bean(name = "noSessionsSecurityContextRepository")
    public SecurityContextRepository noSessionsSecurityContextRepository() {
        HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();
        httpSessionSecurityContextRepository.setDisableUrlRewriting(true);
        httpSessionSecurityContextRepository.setAllowSessionCreation(false);
        return httpSessionSecurityContextRepository;
    }

    @Bean(name = "wmSecurityContextRepositorySuccessHandler")
    public AuthenticationSuccessHandler wmSecurityContextRepositorySuccessHandler() {
        return new WMSecurityContextRepositorySuccessHandler(securityContextRepository());
    }

    @Bean(name = "failureHandler")
    public AuthenticationFailureHandler failureHandler() {
        return new WMApplicationAuthenticationFailureHandler();
    }

    @Bean(name = "rememberMeConfig")
    public RememberMeConfig rememberMeConfig() {
        return new RememberMeConfig();
    }

    @Bean(name = "loginConfig")
    public LoginConfig loginConfig() {
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setSessionTimeout(sessionTimeoutConfig());
        return loginConfig;
    }

    @Bean(name = "sessionTimeoutConfig")
    public SessionTimeoutConfig sessionTimeoutConfig() {
        return new SessionTimeoutConfig();
    }

    @Bean(name = "tokenAuthConfig")
    public TokenAuthConfig tokenAuthConfig() {
        return new TokenAuthConfig();
    }

    @Bean(name = "loginWebProcessFilter")
    public Filter loginWebProcessFilter() {
        return new LoginProcessFilter();
    }

    @Bean(name = "rolesConfig")
    public RolesConfig rolesConfig() {
        return createRoleConfig(roleList());
    }

    /**
     * prefix should be always lowercase,if it is in camelcase @ConfigurationProperties is unable to read the properties
     */
    @Bean
    @ConfigurationProperties(prefix = "security.intercepturls")
    public SecurityInterceptUrlList securityInterceptUrlList() {
        return new SecurityInterceptUrlList();
    }

    @Bean
    @ConfigurationProperties(prefix = "security.approles")
    public RoleList roleList() {
        return new RoleList();
    }

    @Bean(name = "ignoreAntMatchers")
    public WebSecurityCustomizer ignoreAntMatchers() {
        return (web -> {
            String[] defaultAntMatchers = getDefaultAntMatchers();
            WebSecurity.IgnoredRequestConfigurer ignoredRequestConfigurer = web.ignoring();
            for (String ignoreUrl : defaultAntMatchers) {
                ignoredRequestConfigurer.requestMatchers(AntPathRequestMatcher.antMatcher(ignoreUrl));
            }
        });
    }

    @Bean
    public SecurityFilterChain filterChainWithSessions(HttpSecurity http, Filter logoutFilter) throws Exception {
        http
            .csrf().disable()
            .headers().disable()
            .securityMatcher(new NegatedRequestMatcher(new StatelessRequestMatcher(environment.getProperty("security.general.tokenService.parameter"))))
            .sessionManagement().sessionFixation().migrateSession()
            .maximumSessions(environment.getProperty("security.general.login.maxSessionsAllowed", Integer.class))
            .maxSessionsPreventsLogin(false)
            .sessionRegistry(sessionRegistry)
            .and()
            .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
            .requestCache(requestCustomizer ->
                requestCustomizer.requestCache(nullRequestCache()))
            .exceptionHandling()
            .authenticationEntryPoint(appAuthenticationEntryPoint())
            .and()
            .securityContext().securityContextRepository(securityContextRepository())
            .and()
            .authenticationManager(authenticationManager());
        http.authorizeRequests(this::executeUrls)
            .logout().disable()
            .addFilterAt(sessionRepositoryFilter(), ChannelProcessingFilter.class)
            .addFilterAt(wmCsrfFilter(), CsrfFilter.class)
            .addFilterBefore(wmTokenBasedPreAuthenticatedProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class)
            .addFilterAt(logoutFilter, LogoutFilter.class)
            .addFilterAfter(loginWebProcessFilter(), SecurityContextPersistenceFilter.class);
        wmSecurityConfiguration.forEach(securityConfiguration ->
            securityConfiguration.addFilters(http));
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainWithoutSessions(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .headers().disable()
            .securityMatcher(new StatelessRequestMatcher(environment.getProperty("security.general.tokenService.parameter")))
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .requestCache()
            .requestCache(nullRequestCache())
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(appAuthenticationEntryPoint())
            .and()
            .securityContext().securityContextRepository(noSessionsSecurityContextRepository())
            .and()
            .authenticationManager(authenticationManager());
        http.authorizeRequests(this::executeUrls)
            .logout().disable()
            .addFilterBefore(wmTokenBasedPreAuthenticatedProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class);
        wmSecurityConfiguration.forEach(securityConfiguration -> securityConfiguration.addFilters(http));
        return http.build();
    }

    public void addInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        authorizeRequestsCustomizer
            .requestMatchers(AntPathRequestMatcher.antMatcher("/index.html")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/j_spring_security_logout")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/services/application/i18n/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/services/prefabs/**/servicedefs")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/services/security/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/services/servicedefs")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/")).authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/**")).authenticated();
    }

    public void executeUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        try {
            wmSecurityConfiguration.forEach(securityConfiguration -> securityConfiguration.addInterceptUrls(authorizeRequestsCustomizer));
            for (SecurityInterceptUrlEntry customInterceptUrl : securityInterceptUrlList()) {
                if (!customInterceptUrl.getUrlPattern().equals("/**")) {
                    setAntMatchers(authorizeRequestsCustomizer, customInterceptUrl);
                }
            }
            addInterceptUrls(authorizeRequestsCustomizer);
            authorizeRequestsCustomizer.expressionHandler(defaultWebSecurityExpressionHandler());
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    private RolesConfig createRoleConfig(List<Role> roles) {
        RolesConfig rolesConfiguration = new RolesConfig();
        Map<String, RoleConfig> roleMap = new HashMap<>();
        for (Role roleConfig : roles) {
            roleMap.put(roleConfig.getName(), roleConfig.getRoleConfig());
        }
        rolesConfiguration.setRoleMap(roleMap);
        return rolesConfiguration;
    }

    private String[] getDefaultAntMatchers() {
        String ignoreSecurityAntMatchersFileContent = WMIOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("conf/ignore-security-antmatchers.txt"));
        return ignoreSecurityAntMatchersFileContent.split("\n");
    }

    private void setAntMatchers(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer,
                                SecurityInterceptUrlEntry securityInterceptUrlEntry) {
        if (securityInterceptUrlEntry.getHttpMethod() != null) {
            selectAntMatcherWithHttpMethod(authorizeRequestsCustomizer, securityInterceptUrlEntry);
        }
        selectAntMatcherWithNoHttpMethod(authorizeRequestsCustomizer, securityInterceptUrlEntry);
    }

    private void selectAntMatcherWithHttpMethod(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer,
                                                SecurityInterceptUrlEntry securityInterceptUrlEntry) {
        switch (securityInterceptUrlEntry.getPermission()) {
            case Authenticated:
                authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.resolve(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).authenticated();
                break;
            case Role:
                authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.resolve(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).hasAnyRole(securityInterceptUrlEntry.getRoles());
                break;
            default:
                authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.resolve(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).permitAll();
                break;
        }
    }

    private void selectAntMatcherWithNoHttpMethod(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer,
                                                  SecurityInterceptUrlEntry securityInterceptUrlEntry) {
        switch (securityInterceptUrlEntry.getPermission()) {
            case Authenticated:
                authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher(securityInterceptUrlEntry.getUrlPattern())).authenticated();
                break;
            case Role:
                authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher(securityInterceptUrlEntry.getUrlPattern())).hasAnyRole(securityInterceptUrlEntry.getRoles());
                break;
            default:
                authorizeRequestsCustomizer.requestMatchers(AntPathRequestMatcher.antMatcher(securityInterceptUrlEntry.getUrlPattern())).permitAll();
                break;
        }
    }
}