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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
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
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;

import com.wavemaker.app.security.models.CSRFConfig;
import com.wavemaker.app.security.models.CustomFilter;
import com.wavemaker.app.security.models.LoginConfig;
import com.wavemaker.app.security.models.Permission;
import com.wavemaker.app.security.models.RememberMeConfig;
import com.wavemaker.app.security.models.Role;
import com.wavemaker.app.security.models.RoleConfig;
import com.wavemaker.app.security.models.RolesConfig;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.app.security.models.SessionConcurrencyConfig;
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
import com.wavemaker.runtime.security.csrf.handler.WMCsrfTokenRepositorySuccessHandler;
import com.wavemaker.runtime.security.csrf.handler.WMCsrfTokenResponseWriterAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.enabled.configuration.comparator.InterceptUrlComparator;
import com.wavemaker.runtime.security.enabled.configuration.comparator.InterceptUrlStringComparator;
import com.wavemaker.runtime.security.enabled.configuration.models.NamedSecurityFilter;
import com.wavemaker.runtime.security.enabled.configuration.requestmatcher.StatelessRequestMatcher;
import com.wavemaker.runtime.security.entrypoint.WMCompositeAuthenticationEntryPoint;
import com.wavemaker.runtime.security.filter.WMTokenBasedPreAuthenticatedProcessingFilter;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationRedirectionHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessRedirectionHandler;
import com.wavemaker.runtime.security.handler.WMSecurityContextRepositorySuccessHandler;
import com.wavemaker.runtime.security.model.FilterInfo;
import com.wavemaker.runtime.security.token.WMTokenBasedAuthenticationService;
import com.wavemaker.runtime.security.token.repository.TokenRepository;
import com.wavemaker.runtime.security.token.repository.WMTokenRepository;
import com.wavemaker.runtime.webprocess.filter.LoginProcessFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Conditional(SecurityEnabledCondition.class)
public class SecurityEnabledBaseConfiguration {
    @Autowired
    @Lazy
    private List<AuthenticationProvider> authenticationProvidersList;

    @Autowired
    @Lazy
    private List<WMSecurityConfiguration> wmSecurityConfigurationList;

    @Autowired
    @Lazy
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    @Lazy
    private SessionRegistry sessionRegistry;

    @Autowired
    private Environment environment;
    @Autowired
    private ApplicationContext applicationContext;

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
    public CookieSerializer defaultCookieSerializer(LoginConfig loginConfig) {
        int cookieMaxAge = loginConfig.getCookieMaxAge();
        String cookiePath = loginConfig.getCookiePath();
        boolean base64Encode = loginConfig.isCookieBase64Encode();
        String jvmRoute = environment.getProperty("security.general.cookie.jvmRoute");
        String sameSite = environment.getProperty("security.general.cookie.sameSite");
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        if (StringUtils.isNotBlank(cookiePath)) {
            defaultCookieSerializer.setCookiePath(cookiePath);
        }
        if (StringUtils.isNotBlank(jvmRoute)) {
            defaultCookieSerializer.setJvmRoute(jvmRoute);
        }
        if (StringUtils.isNotBlank(sameSite)) {
            defaultCookieSerializer.setSameSite(sameSite);
        } else {
            defaultCookieSerializer.setSameSite(null);
        }
        defaultCookieSerializer.setCookieMaxAge((int) TimeUnit.MINUTES.toSeconds(cookieMaxAge));
        defaultCookieSerializer.setUseBase64Encoding(base64Encode);
        return defaultCookieSerializer;
    }

    @Bean(name = "httpSessionIdResolver")
    public HttpSessionIdResolver httpSessionIdResolver(CookieSerializer defaultCookieSerializer) {
        CookieHttpSessionIdResolver cookieHttpSessionIdResolver = new CookieHttpSessionIdResolver();
        cookieHttpSessionIdResolver.setCookieSerializer(defaultCookieSerializer);
        return cookieHttpSessionIdResolver;
    }

    @Bean(name = "sessionRepositoryFilter")
    public Filter sessionRepositoryFilter(HttpSessionIdResolver httpSessionIdResolver) {
        SessionRepositoryFilter<? extends Session> sessionRepositoryFilter = new SessionRepositoryFilter<>(sessionRepository);
        sessionRepositoryFilter.setHttpSessionIdResolver(httpSessionIdResolver);
        return sessionRepositoryFilter;
    }

    @Bean(name = "wmcsrfFilter")
    public Filter wmCsrfFilter(CsrfTokenRepository csrfTokenRepository, RequestMatcher csrfSecurityRequestMatcher) {
        return new WMCsrfFilter(csrfTokenRepository, csrfSecurityRequestMatcher);
    }

    //TODO Change the return type to generic to override beans from xml
    @Bean(name = "appAuthenticationEntryPoint")
    public WMCompositeAuthenticationEntryPoint appAuthenticationEntryPoint() {
        return new WMCompositeAuthenticationEntryPoint();
    }

    @Bean(name = "sessionFixationProtectionStrategy")
    public SessionAuthenticationStrategy sessionFixationProtectionStrategy() {
        return new SessionFixationProtectionStrategy();
    }

    @Bean(name = "csrfAuthenticationStrategy")
    public SessionAuthenticationStrategy csrfAuthenticationStrategy(CsrfTokenRepository csrfTokenRepository) {
        return new CsrfAuthenticationStrategy(csrfTokenRepository);
    }

    @Bean(name = "compositeSessionAuthenticationStrategy")
    public SessionAuthenticationStrategy compositeSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionFixationProtectionStrategy,
                                                                                SessionAuthenticationStrategy csrfAuthenticationStrategy,
                                                                                LoginConfig loginConfig) {
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(loginConfig.getSessionConcurrencyConfig().getMaxSessionsAllowed());
        concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(false);
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>(Arrays.asList(concurrentSessionControlAuthenticationStrategy, registerSessionAuthenticationStrategy, sessionFixationProtectionStrategy, csrfAuthenticationStrategy));
        return new CompositeSessionAuthenticationStrategy(delegateStrategies);
    }

    @Bean(name = "wmAppAccessDeniedHandler")
    public AccessDeniedHandler wmAppAccessDeniedHandler() {
        return new WMAppAccessDeniedHandler();
    }

    @Bean(name = "csrfTokenRepository")
    public CsrfTokenRepository csrfTokenRepository(CsrfTokenRepository wmHttpSessionCsrfTokenRepository) {
        return new WMCsrfTokenRepository(wmHttpSessionCsrfTokenRepository);
    }

    @Bean(name = "csrfConfig")
    public CSRFConfig csrfConfig() {
        return new CSRFConfig();
    }

    @Bean(name = "wmHttpSessionCsrfTokenRepository")
    public CsrfTokenRepository wmHttpSessionCsrfTokenRepository(CSRFConfig csrfConfig) {
        WMHttpSessionCsrfTokenRepository wmHttpSessionCsrfTokenRepository = new WMHttpSessionCsrfTokenRepository();
        wmHttpSessionCsrfTokenRepository.setCsrfConfig(csrfConfig);
        return wmHttpSessionCsrfTokenRepository;
    }

    @Bean(name = "csrfSecurityRequestMatcher")
    public RequestMatcher csrfSecurityRequestMatcher(CSRFConfig csrfConfig) {
        CsrfSecurityRequestMatcher requestMatcher = new CsrfSecurityRequestMatcher();
        requestMatcher.setCsrfConfig(csrfConfig);
        return requestMatcher;
    }

    @Bean(name = "wmCsrfLogoutHandler")
    public LogoutHandler wmCsrfLogoutHandler(LogoutHandler csrfLogoutHandler) {
        return new WMCsrfLogoutHandler(csrfLogoutHandler);
    }

    @Bean(name = "csrfLogoutHandler")
    public LogoutHandler csrfLogoutHandler(CsrfTokenRepository csrfTokenRepository) {
        return new CsrfLogoutHandler(csrfTokenRepository);
    }

    @Bean(name = "logoutSuccessHandler")
    public LogoutSuccessHandler logoutSuccessHandler(RedirectStrategy redirectStrategyBean) {
        SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        simpleUrlLogoutSuccessHandler.setDefaultTargetUrl("/");
        simpleUrlLogoutSuccessHandler.setRedirectStrategy(redirectStrategyBean);
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
    public Filter wmTokenBasedPreAuthenticatedProcessingFilter
        (AuthenticationManager authenticationManager, WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService) {
        return new WMTokenBasedPreAuthenticatedProcessingFilter(authenticationManager, wmTokenBasedAuthenticationService);
    }

    @Bean(name = "tokenRepository")
    public TokenRepository tokenRepository() {
        return new WMTokenRepository();
    }

    @Bean(name = "wmTokenBasedAuthenticationService")
    public WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService() {
        return new WMTokenBasedAuthenticationService();
    }

    @Bean(name = "successHandler")
    public AuthenticationSuccessHandler successHandler(AuthenticationSuccessHandler wmCsrfTokenRepositorySuccessHandler,
                                                       AuthenticationSuccessHandler wmCsrfTokenResponseWriterAuthenticationSuccessHandler,
                                                       WMAuthenticationRedirectionHandler wmAuthenticationSuccessRedirectionHandler,
                                                       AuthenticationSuccessHandler wmSecurityContextRepositorySuccessHandler) {
        List<AuthenticationSuccessHandler> defaultSuccessHandlerList = new ArrayList<>();
        defaultSuccessHandlerList.add(wmSecurityContextRepositorySuccessHandler);
        defaultSuccessHandlerList.add(wmCsrfTokenRepositorySuccessHandler);
        defaultSuccessHandlerList.add(wmCsrfTokenResponseWriterAuthenticationSuccessHandler);
        WMApplicationAuthenticationSuccessHandler wmApplicationAuthenticationSuccessHandler = new WMApplicationAuthenticationSuccessHandler();
        wmApplicationAuthenticationSuccessHandler.setDefaultSuccessHandlerList(defaultSuccessHandlerList);
        wmApplicationAuthenticationSuccessHandler.setAuthenticationSuccessRedirectionHandler(wmAuthenticationSuccessRedirectionHandler);
        return wmApplicationAuthenticationSuccessHandler;
    }

    @Bean(name = "wmAuthenticationSuccessRedirectionHandler")
    public WMAuthenticationRedirectionHandler wmAuthenticationSuccessRedirectionHandler() {
        return new WMAuthenticationSuccessRedirectionHandler();
    }

    @Bean(name = "wmCsrfTokenRepositorySuccessHandler")
    public AuthenticationSuccessHandler wmCsrfTokenRepositorySuccessHandler(CsrfTokenRepository csrfTokenRepository) {
        return new WMCsrfTokenRepositorySuccessHandler(csrfTokenRepository);
    }

    @Bean(name = "wmCsrfTokenResponseWriterAuthenticationSuccessHandler")
    public AuthenticationSuccessHandler wmCsrfTokenResponseWriterAuthenticationSuccessHandler(CsrfTokenRepository csrfTokenRepository) {
        return new WMCsrfTokenResponseWriterAuthenticationSuccessHandler(csrfTokenRepository);
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
    public AuthenticationSuccessHandler wmSecurityContextRepositorySuccessHandler(SecurityContextRepository securityContextRepository) {
        return new WMSecurityContextRepositorySuccessHandler(securityContextRepository);
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
    public LoginConfig loginConfig(SessionTimeoutConfig sessionTimeoutConfig, SessionConcurrencyConfig sessionConcurrencyConfig) {
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setSessionTimeout(sessionTimeoutConfig);
        loginConfig.setSessionConcurrencyConfig(sessionConcurrencyConfig);
        return loginConfig;
    }

    @Bean(name = "sessionTimeoutConfig")
    public SessionTimeoutConfig sessionTimeoutConfig() {
        return new SessionTimeoutConfig();
    }

    @Bean(name = "sessionConcurrencyConfig")
    public SessionConcurrencyConfig sessionConcurrencyConfig() {
        return new SessionConcurrencyConfig();
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
    public List<SecurityInterceptUrlEntry> securityInterceptUrlList() {
        return new ArrayList<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "security.approles")
    public List<Role> roleList() {
        return new ArrayList<>();
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
    @ConfigurationProperties(prefix = "security.general.customfilters")
    public List<CustomFilter> customFilterList() {
        return new ArrayList<>();
    }

    @Bean
    public SecurityFilterChain filterChainWithSessions(HttpSecurity http, Filter logoutFilter, AuthenticationManager authenticationManager,
                                                       Filter sessionRepositoryFilter, Filter wmcsrfFilter,
                                                       WMCompositeAuthenticationEntryPoint appAuthenticationEntryPoint,
                                                       RequestCache nullRequestCache,
                                                       SecurityContextRepository securityContextRepository,
                                                       Filter loginWebProcessFilter) throws Exception {
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
                requestCustomizer.requestCache(nullRequestCache))
            .exceptionHandling()
            .authenticationEntryPoint(appAuthenticationEntryPoint)
            .and()
            .securityContext().securityContextRepository(securityContextRepository)
            .and()
            .authenticationManager(authenticationManager)
            .authorizeRequests(this::authorizeHttpRequests)
            .logout().disable()
            .addFilterAt(sessionRepositoryFilter, DisableEncodeUrlFilter.class)
            .addFilterAt(wmcsrfFilter, CsrfFilter.class)
            .addFilterAt(logoutFilter, LogoutFilter.class)
            .addFilterAfter(loginWebProcessFilter, SecurityContextPersistenceFilter.class);
        addFilters(http);
        addCustomFilters(http);
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainWithoutSessions(HttpSecurity http, Filter wmTokenBasedPreAuthenticatedProcessingFilter,
                                                          AuthenticationManager authenticationManager,
                                                          WMCompositeAuthenticationEntryPoint appAuthenticationEntryPoint,
                                                          RequestCache nullRequestCache,
                                                          SecurityContextRepository noSessionsSecurityContextRepository) throws Exception {
        http
            .csrf().disable()
            .headers().disable()
            .securityMatcher(new StatelessRequestMatcher(environment.getProperty("security.general.tokenService.parameter")))
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .requestCache()
            .requestCache(nullRequestCache)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(appAuthenticationEntryPoint)
            .and()
            .securityContext().securityContextRepository(noSessionsSecurityContextRepository)
            .and()
            .authenticationManager(authenticationManager)
            .authorizeRequests(this::authorizeHttpRequests)
            .logout().disable()
            .addFilterBefore(wmTokenBasedPreAuthenticatedProcessingFilter, AbstractPreAuthenticatedProcessingFilter.class);
        addFilters(http);
        addCustomFilters(http);
        return http.build();
    }

    @Bean(name = "filterSecurityInterceptor")
    public FilterSecurityInterceptor filterSecurityInterceptor(SecurityFilterChain filterChainWithSessions) {
        return (FilterSecurityInterceptor) filterChainWithSessions.getFilters().stream().filter(FilterSecurityInterceptor.class::isInstance).findFirst().orElseThrow();
    }

    private void addFilters(HttpSecurity http) {
        List<FilterInfo> filterChainFilters = new ArrayList<>();
        for (WMSecurityConfiguration wmSecurityConfiguration : wmSecurityConfigurationList) {
            filterChainFilters.addAll(wmSecurityConfiguration.getFilters());
        }
        for (FilterInfo filterInfo : filterChainFilters) {
            addFiltersToFilterChain(http, filterInfo);
        }
    }

    private void addFiltersToFilterChain(HttpSecurity http, FilterInfo filterInfo) {
        switch (filterInfo.getPosition()) {
            case "at":
                http.addFilterAt((Filter) applicationContext.getBean(filterInfo.getFilterBeanName()), filterInfo.getFilterClass());
                break;
            case "before":
                http.addFilterBefore((Filter) applicationContext.getBean(filterInfo.getFilterBeanName()), filterInfo.getFilterClass());
                break;
            case "after":
                http.addFilterAfter((Filter) applicationContext.getBean(filterInfo.getFilterBeanName()), filterInfo.getFilterClass());
                break;
            default:
                throw new WMRuntimeException("Filter position can only be any one of these 'at/before/after', but position given as : " +
                    filterInfo.getPosition());
        }
    }

    private void authorizeHttpRequests(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
        List<SecurityInterceptUrlEntry> securityInterceptUrlEntryList = new ArrayList<>();
        for (WMSecurityConfiguration wmSecurityConfiguration : wmSecurityConfigurationList) {
            securityInterceptUrlEntryList.addAll(wmSecurityConfiguration.getSecurityInterceptUrls());
        }
        securityInterceptUrlEntryList.addAll(securityInterceptUrlList());
        securityInterceptUrlEntryList.addAll(getDefaultSecurityInterceptUrlEntryList());
        securityInterceptUrlEntryList.sort(new InterceptUrlComparator());
        securityInterceptUrlEntryList.sort(new InterceptUrlStringComparator());
        for (SecurityInterceptUrlEntry securityInterceptUrlEntry : securityInterceptUrlEntryList) {
            setAntMatchers(registry, securityInterceptUrlEntry);
        }
    }

    private List<SecurityInterceptUrlEntry> getDefaultSecurityInterceptUrlEntryList() {
        return List.of(new SecurityInterceptUrlEntry("/index.html", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/j_spring_security_logout", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/services/application/i18n/**", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/services/prefabs/**/servicedefs", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/services/security/**", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/services/servicedefs", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/**", com.wavemaker.app.web.http.HttpMethod.OPTIONS, Permission.PermitAll),
            new SecurityInterceptUrlEntry("/", Permission.Authenticated),
            new SecurityInterceptUrlEntry("/**", Permission.Authenticated));
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

    private void setAntMatchers(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry,
                                SecurityInterceptUrlEntry securityInterceptUrlEntry) {
        if (securityInterceptUrlEntry.getHttpMethod() != null) {
            selectAntMatcherWithHttpMethod(registry, securityInterceptUrlEntry);
        } else {
            selectAntMatcherWithNoHttpMethod(registry, securityInterceptUrlEntry);
        }
    }

    private void selectAntMatcherWithHttpMethod(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry,
                                                SecurityInterceptUrlEntry securityInterceptUrlEntry) {
        switch (securityInterceptUrlEntry.getPermission()) {
            case Authenticated:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.resolve(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).authenticated();
                break;
            case Role:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.resolve(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).hasAnyRole(securityInterceptUrlEntry.getRoles());
                break;
            case PermitAll:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.resolve(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).permitAll();
                break;
            default:
                throw new IllegalStateException("Expected any one of permissions for intercept url : " +
                    securityInterceptUrlEntry.getUrlPattern() + " as Authenticated/PermitAll/Role");
        }
    }

    private void selectAntMatcherWithNoHttpMethod(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry,
                                                  SecurityInterceptUrlEntry securityInterceptUrlEntry) {
        switch (securityInterceptUrlEntry.getPermission()) {
            case Authenticated:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(securityInterceptUrlEntry.getUrlPattern())).authenticated();
                break;
            case Role:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(securityInterceptUrlEntry.getUrlPattern())).hasAnyRole(securityInterceptUrlEntry.getRoles());
                break;
            case PermitAll:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(securityInterceptUrlEntry.getUrlPattern())).permitAll();
                break;
            default:
                throw new IllegalStateException("Expected any one of permissions for intercept url : " +
                    securityInterceptUrlEntry.getUrlPattern() + " as Authenticated/PermitAll/Role");
        }
    }

    private void addCustomFilters(HttpSecurity http) {
        List<CustomFilter> customFilters = customFilterList();
        customFilters.forEach(customFilter -> {

            FilterInfo customFilterInfo = null;
            String ref = customFilter.getRef();
            if (StringUtils.isBlank(ref)) {
                throw new IllegalStateException("ref cannot be empty");
            }

            AtomicInteger count = new AtomicInteger();
            NamedSecurityFilter before = getNamedSecurityFilter(customFilter.getBefore(), count);
            NamedSecurityFilter position = getNamedSecurityFilter(customFilter.getPosition(), count);
            NamedSecurityFilter after = getNamedSecurityFilter(customFilter.getAfter(), count);
            if (count.get() != 1) {
                throw new IllegalStateException("Expected one and only one of before/after/position parameter to be set to the custom filter");
            }
            if (before != null) {
                customFilterInfo = new FilterInfo((Class<? extends Filter>) NamedSecurityFilter.getClass(customFilter.getBefore()),
                    ref, "before");
            }
            if (after != null) {
                customFilterInfo = new FilterInfo((Class<? extends Filter>) NamedSecurityFilter.getClass(customFilter.getAfter()),
                    ref, "after");
            }
            if (position != null) {
                customFilterInfo = new FilterInfo((Class<? extends Filter>) NamedSecurityFilter.getClass(customFilter.getPosition()),
                    ref, "at");
            }
            addFiltersToFilterChain(http, customFilterInfo);
        });
    }

    private NamedSecurityFilter getNamedSecurityFilter(String str, AtomicInteger count) {
        if (StringUtils.isNotBlank(str)) {
            count.incrementAndGet();
            return NamedSecurityFilter.getValue(str);
        }
        return null;
    }
}