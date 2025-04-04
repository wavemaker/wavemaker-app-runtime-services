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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.Filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.FilterInvocation;
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
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
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
import com.wavemaker.runtime.security.authenticationprovider.WMDelegatingAuthenticationProvider;
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
import com.wavemaker.runtime.security.entrypoint.AuthenticationEntryPointRegistry;
import com.wavemaker.runtime.security.entrypoint.WMCompositeAuthenticationEntryPoint;
import com.wavemaker.runtime.security.filter.WMRequestResponseHolderFilter;
import com.wavemaker.runtime.security.filter.WMTokenBasedPreAuthenticatedProcessingFilter;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationRedirectionHandler;
import com.wavemaker.runtime.security.handler.WMAuthenticationSuccessRedirectionHandler;
import com.wavemaker.runtime.security.handler.logout.LogoutSuccessHandlerRegistry;
import com.wavemaker.runtime.security.handler.logout.WMApplicationLogoutSuccessHandler;
import com.wavemaker.runtime.security.provider.saml.BrowserDelegatingLogoutFilter;
import com.wavemaker.runtime.security.token.WMTokenBasedAuthenticationService;
import com.wavemaker.runtime.security.token.repository.TokenRepository;
import com.wavemaker.runtime.security.token.repository.WMTokenRepository;
import com.wavemaker.runtime.webprocess.filter.LoginProcessFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Conditional(SecurityEnabledCondition.class)
public class SecurityEnabledBaseConfiguration {

    @Autowired(required = false)
    @Lazy
    private List<WMDelegatingAuthenticationProvider> authenticationProvidersList;

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

    @Autowired(required = false)
    private PersistentTokenBasedRememberMeServices rememberMeServices;

    @Value("${security.general.rememberMe.enabled:false}")
    private boolean rememberMeEnabled;

    @Bean(name = "defaultWebSecurityExpressionHandler")
    public SecurityExpressionHandler<FilterInvocation> defaultWebSecurityExpressionHandler() {
        return new DefaultWebSecurityExpressionHandler();
    }

    @Bean(name = "authenticationManager")
    public AuthenticationManager authenticationManager() {
        try {
            return authenticationProvidersList.isEmpty() ?
                new ProviderManager(new AuthenticationManagerBeanDefinitionParser.NullAuthenticationProvider()) :
                new ProviderManager(authenticationProvidersList.stream().map(authenticationProvider -> (AuthenticationProvider) authenticationProvider).toList());
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
    }

    @Bean(name = "defaultCookieSerializer")
    public CookieSerializer defaultCookieSerializer() {
        LoginConfig loginConfig = loginConfig();
        int cookieMaxAge = loginConfig.getCookieMaxAge();
        String cookiePath = loginConfig.getCookiePath();
        boolean base64Encode = loginConfig.isCookieBase64Encode();
        String jvmRoute = loginConfig.getJvmRoute();
        String sameSite = loginConfig.getSameSite();
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

    @Bean(name = "wmRequestResponseHolderFilter")
    public Filter wmRequestResponseHolderFiler() {
        return new WMRequestResponseHolderFilter();
    }

    @Bean(name = "wmcsrfFilter")
    public Filter wmCsrfFilter() {
        return new WMCsrfFilter(csrfTokenRepository(), csrfSecurityRequestMatcher());
    }

    @Bean(name = "appAuthenticationEntryPoint")
    public AuthenticationEntryPoint appAuthenticationEntryPoint() {
        return new WMCompositeAuthenticationEntryPoint();
    }

    @Bean(name = "authenticationEntryPointRegistry")
    public AuthenticationEntryPointRegistry authenticationEntryPointRegistry() {
        return new AuthenticationEntryPointRegistry();
    }

    @Bean(name = "sessionFixationProtectionStrategy")
    public SessionAuthenticationStrategy sessionFixationProtectionStrategy() {
        return new SessionFixationProtectionStrategy();
    }

    @Bean(name = "csrfAuthenticationStrategy")
    public SessionAuthenticationStrategy csrfAuthenticationStrategy() {
        return new CsrfAuthenticationStrategy(csrfTokenRepository());
    }

    @Bean(name = "compositeSessionAuthenticationStrategy")
    public SessionAuthenticationStrategy compositeSessionAuthenticationStrategy() {
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(loginConfig().getSessionConcurrencyConfig().getMaxSessionsAllowed());
        concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(false);
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        List<SessionAuthenticationStrategy> delegateStrategies = Arrays.asList(concurrentSessionControlAuthenticationStrategy, registerSessionAuthenticationStrategy, sessionFixationProtectionStrategy(), csrfAuthenticationStrategy());
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

    @Bean(name = "tokenRepository")
    public TokenRepository tokenRepository() {
        return new WMTokenRepository();
    }

    @Bean(name = "wmTokenBasedAuthenticationService")
    public WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService() {
        return new WMTokenBasedAuthenticationService();
    }

    @Bean(name = "successHandler")
    public AuthenticationSuccessHandler successHandler() {
        List<AuthenticationSuccessHandler> defaultSuccessHandlerList = new ArrayList<>();
        defaultSuccessHandlerList.add(wmCsrfTokenRepositorySuccessHandler());
        defaultSuccessHandlerList.add(wmCsrfTokenResponseWriterAuthenticationSuccessHandler());
        WMApplicationAuthenticationSuccessHandler wmApplicationAuthenticationSuccessHandler = new WMApplicationAuthenticationSuccessHandler();
        wmApplicationAuthenticationSuccessHandler.setDefaultSuccessHandlerList(defaultSuccessHandlerList);
        wmApplicationAuthenticationSuccessHandler.setAuthenticationSuccessRedirectionHandler(wmAuthenticationSuccessRedirectionHandler());
        return wmApplicationAuthenticationSuccessHandler;
    }

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
        loginConfig.setSessionConcurrencyConfig(sessionConcurrencyConfig());
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
    @ConfigurationProperties(prefix = "security.intercept-urls")
    public List<SecurityInterceptUrlEntry> securityInterceptUrlList() {
        return new ArrayList<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "security.app-roles")
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
    @ConfigurationProperties(prefix = "security.general.custom-filters")
    public List<CustomFilter> customFilterList() {
        return new ArrayList<>();
    }

    @Bean
    public SecurityFilterChain filterChainWithSessions(HttpSecurity http, Filter logoutFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .headers(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagementConfigurer ->
                sessionManagementConfigurer.sessionFixation().
                    migrateSession().
                    sessionConcurrency(sessionConcurrencyConfigurer ->
                        sessionConcurrencyConfigurer.
                            maximumSessions(environment.getProperty("security.general.login.maxSessionsAllowed", Integer.class)).
                            maxSessionsPreventsLogin(false).
                            sessionRegistry(sessionRegistry)))
            .requestCache(requestCustomizer ->
                requestCustomizer.requestCache(nullRequestCache()))
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(appAuthenticationEntryPoint()))
            .securityContext(securityContext -> securityContext.
                securityContextRepository(securityContextRepository()))
            .authenticationManager(authenticationManager())
            .authorizeRequests(this::authorizeHttpRequests)
            .logout(AbstractHttpConfigurer::disable)
            .addFilterAt(sessionRepositoryFilter(), DisableEncodeUrlFilter.class)
            .addFilterAt(wmRequestResponseHolderFiler(), DisableEncodeUrlFilter.class)
            .addFilterAt(wmCsrfFilter(), CsrfFilter.class)
            .addFilterAt(logoutFilter, LogoutFilter.class)
            .addFilterAfter(loginWebProcessFilter(), SecurityContextHolderFilter.class)
            .addFilterBefore(wmTokenBasedPreAuthenticatedProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class);
        wmSecurityConfigurationList.forEach(securityConfiguration -> {
            securityConfiguration.addFilters(http);
            securityConfiguration.addStatelessFilters(http);
        });
        addCustomFilters(http);
        return http.build();
    }

    @Bean(name = "filterSecurityInterceptor")
    public FilterSecurityInterceptor filterSecurityInterceptor(SecurityFilterChain filterChainWithSessions) {
        return (FilterSecurityInterceptor) filterChainWithSessions.getFilters().stream().filter(FilterSecurityInterceptor.class::isInstance).findFirst().orElseThrow();
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutHandler securityContextLogoutHandler, LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter;
        if (rememberMeEnabled && rememberMeServices != null) {
            logoutFilter = new BrowserDelegatingLogoutFilter(wmApplicationLogoutSuccessHandler(), securityContextLogoutHandler, wmCsrfLogoutHandler, rememberMeServices);
        } else {
            logoutFilter = new BrowserDelegatingLogoutFilter(wmApplicationLogoutSuccessHandler(), securityContextLogoutHandler, wmCsrfLogoutHandler);
        }
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }

    @Bean(name = "logoutSuccessHandler")
    public LogoutSuccessHandler wmApplicationLogoutSuccessHandler() {
        return new WMApplicationLogoutSuccessHandler();
    }

    @Bean(name = "logoutSuccessHandlerRegistry")
    public LogoutSuccessHandlerRegistry logoutSuccessHandlerRegistry() {
        return new LogoutSuccessHandlerRegistry();
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
            new SecurityInterceptUrlEntry("/services/webprocess/**", Permission.PermitAll),
            new SecurityInterceptUrlEntry("/**", com.wavemaker.app.web.http.HttpMethod.OPTIONS, Permission.PermitAll),
            new SecurityInterceptUrlEntry("/", Permission.Authenticated),
            new SecurityInterceptUrlEntry("/**", Permission.Authenticated));
    }

    private RolesConfig createRoleConfig(List<Role> roles) {
        RolesConfig rolesConfiguration = new RolesConfig();
        Map<String, RoleConfig> roleMap = new LinkedHashMap<>();
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
                    Objects.requireNonNull(HttpMethod.valueOf(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).authenticated();
                break;
            case Role:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.valueOf(securityInterceptUrlEntry.getHttpMethod().name())),
                    securityInterceptUrlEntry.getUrlPattern())).hasAnyRole(securityInterceptUrlEntry.getRoles());
                break;
            case PermitAll:
                registry.requestMatchers(AntPathRequestMatcher.antMatcher(
                    Objects.requireNonNull(HttpMethod.valueOf(securityInterceptUrlEntry.getHttpMethod().name())),
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

            String ref = customFilter.getRef();
            if (StringUtils.isBlank(ref)) {
                throw new IllegalStateException("ref cannot be empty");
            }
            Object bean = applicationContext.getBean(ref);

            AtomicInteger count = new AtomicInteger();
            NamedSecurityFilter before = getNamedSecurityFilter(customFilter.getBefore(), count);
            NamedSecurityFilter position = getNamedSecurityFilter(customFilter.getPosition(), count);
            NamedSecurityFilter after = getNamedSecurityFilter(customFilter.getAfter(), count);
            if (count.get() != 1) {
                throw new IllegalStateException("Expected one and only one of before/after/position parameter to be set to the custom filter");
            }
            if (before != null) {
                http.addFilterBefore((Filter) bean, (Class<? extends Filter>) NamedSecurityFilter.getClass(customFilter.getBefore()));
            }
            if (after != null) {
                http.addFilterAfter((Filter) bean, (Class<? extends Filter>) NamedSecurityFilter.getClass(customFilter.getAfter()));
            }
            if (position != null) {
                http.addFilterAt((Filter) bean, (Class<? extends Filter>) NamedSecurityFilter.getClass(customFilter.getPosition()));
            }
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