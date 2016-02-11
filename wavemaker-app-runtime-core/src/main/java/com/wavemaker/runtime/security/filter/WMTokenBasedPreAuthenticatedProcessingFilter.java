package com.wavemaker.runtime.security.filter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.runtime.security.token.Token;
import com.wavemaker.runtime.security.token.WMTokenBasedAuthenticationService;

/**
 * WMAppTokenBasedPreAuthenticatedProcessingFilter for processing filters that handle pre-authenticated authentication requests, where it is assumed
 * that the principal has already been authenticated by an external system.
 * <p/>
 * The purpose is then only to extract the necessary information on the principal from the incoming request query param, rather
 * than to authenticate them. External authentication systems may provide this information via request data such as
 * token which the pre-authentication system can extract. It is assumed that the external system is
 * responsible for the accuracy of the data and preventing the submission of forged values.
 * <p/>
 * This filter will extract principal from the request only if url contains a "token" param.
 * If token exist then it will extract principal from the given token and build authentication object.
 * <p/>
 * eg, http://host/projectName/services/pages/page?name="abc"&token="YWRtaW46MTQ1NjEyMDMxMTY5ODpjOWUxZTRiYmFiYWQ0MTdhOGNiMWYyNTI1NmY2MTJlMg"
 *
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 8/2/16
 */
public class WMTokenBasedPreAuthenticatedProcessingFilter extends GenericFilterBean implements
        ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(WMTokenBasedPreAuthenticatedProcessingFilter.class);

    private AuthenticationManager authenticationManager;
    private WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService;
    private ApplicationEventPublisher eventPublisher = null;

    public static final String WM_AUTH_TOKEN_NAME = "wm_auth_token";
    private boolean continueFilterChainOnUnsuccessfulAuthentication = true;


    public WMTokenBasedPreAuthenticatedProcessingFilter() {
    }

    public WMTokenBasedPreAuthenticatedProcessingFilter(final AuthenticationManager authenticationManager, final WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService) {
        this.authenticationManager = authenticationManager;
        this.wmTokenBasedAuthenticationService = wmTokenBasedAuthenticationService;
    }

    /**
     * Check whether all required properties have been set.
     */
    @Override
    public void afterPropertiesSet() {

        try {
            super.afterPropertiesSet();
        } catch (ServletException e) {
            // convert to RuntimeException for passivity on afterPropertiesSet signature
            throw new RuntimeException(e);
        }
        Assert.notNull(this.authenticationManager, "An AuthenticationManager must be set");
        Assert.notNull(this.wmTokenBasedAuthenticationService, "An AccessTokenBasedAuthenticationService must be set");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        LOGGER.debug("Checking secure context token: [{}]", SecurityContextHolder.getContext().getAuthentication());

        if (requiresAuthentication((HttpServletRequest) request)) {
            String token = getTokenFromReq((HttpServletRequest) request);
            doAuthenticate(token, (HttpServletRequest) request, (HttpServletResponse) response);
        }

        chain.doFilter(request, response);
    }

    private void doAuthenticate(final String token, final HttpServletRequest request, final HttpServletResponse response) {

        try {
            Authentication authentication = wmTokenBasedAuthenticationService.getAuthentication(new Token(token));

            if (authentication == null) {
                LOGGER.debug("No pre-authenticated principal found in request");
                return;
            }
            successfulAuthentication(request, response, authentication);
        } catch (AuthenticationException failed) {
            unsuccessfulAuthentication(request, response, failed);

            if (!continueFilterChainOnUnsuccessfulAuthentication) {
                throw failed;
            }
        }
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        return  currentUser == null;
    }

    protected String getTokenFromReq(final HttpServletRequest request) {
        String token;
        try {
            token = getQueryParamValue(request, WM_AUTH_TOKEN_NAME);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    /**
     * Puts the <code>Authentication</code> instance returned by the
     * authentication manager into the secure context.
     */
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success: " + authResult);
        }
        SecurityContextHolder.getContext().setAuthentication(authResult);
        // Fire event
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }
    }

    /**
     * Ensures the authentication object in the secure context is set to null when authentication fails.
     * <p/>
     * Caches the failure exception as a request attribute
     */
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        SecurityContextHolder.clearContext();

        if (logger.isDebugEnabled()) {
            logger.debug("Cleared security context due to exception", failed);
        }
        request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, failed);
    }

    private String getQueryParamValue(final HttpServletRequest request, final String param) throws URISyntaxException {
        Map parameterMap = request.getParameterMap();
        Set<Map.Entry> entrySet = parameterMap.entrySet();
        Iterator<Map.Entry> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> entry = iterator.next();
            if (param.equals(entry.getKey())) {
                return entry.getValue()[0];
            }
        }
        return null;
    }

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {

    }

    /**
     * @param authenticationManager The AuthenticationManager to use
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * If set to {@code true}, any {@code AuthenticationException} raised by the {@code AuthenticationManager} will be
     * swallowed, and the request will be allowed to proceed, potentially using alternative authentication mechanisms.
     * If {@code false} (the default), authentication failure will result in an immediate exception.
     *
     * @param continueFilterChainOnUnsuccessfulAuthentication set to {@code true} to allow the request to proceed after a failed authentication.
     */
    public void setContinueFilterChainOnUnsuccessfulAuthentication(boolean continueFilterChainOnUnsuccessfulAuthentication) {
        continueFilterChainOnUnsuccessfulAuthentication = continueFilterChainOnUnsuccessfulAuthentication;
    }

    public void setWmTokenBasedAuthenticationService(final WMTokenBasedAuthenticationService wmTokenBasedAuthenticationService) {
        this.wmTokenBasedAuthenticationService = wmTokenBasedAuthenticationService;
    }

}
