package com.wavemaker.runtime.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

public class ContentSecurityPolicyFilter extends GenericFilterBean {

    @Value("${security.general.csp.enabled}")
    private boolean cspEnabled;

    @Value("${security.general.csp.policy}")
    private String cspPolicy;

    private boolean nonceReplacementNeeded;

    private static final String NONCE_VALUE_PATTERN = "\\$\\{NONCE_VALUE\\}";
    private static final String NONCE_PLACEHOLDER = "${NONCE_VALUE}";
    private static final String CSP_HEADER = "Content-Security-Policy";
    private static final Logger logger = LoggerFactory.getLogger(ContentSecurityPolicyFilter.class);

    private AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/index.html");

    @Override
    protected void initFilterBean() {
        if (cspEnabled) {
            if (StringUtils.isNotBlank(cspPolicy)) {
                nonceReplacementNeeded = cspPolicy.contains(NONCE_PLACEHOLDER);
                logger.info("csp policy is {} and nonceReplacementNeeded:{}", cspPolicy, nonceReplacementNeeded);
            } else {
                logger.warn("Disabling csp as cspPolicy is blank");
                cspEnabled = false;
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (cspEnabled && requestMatches(httpServletRequest)) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            if (nonceReplacementNeeded) {
                String nonce = generateRandomNonce(12);
                httpServletResponse.addHeader(CSP_HEADER, cspPolicy.replace(NONCE_PLACEHOLDER, nonce));
                CSPResponseWrapper cspResponseWrapper = new CSPResponseWrapper(httpServletResponse);
                chain.doFilter(httpServletRequest, cspResponseWrapper);
                String res = new String(cspResponseWrapper.getByteArray());
                res = res.replaceAll(NONCE_VALUE_PATTERN, nonce);
                httpServletResponse.setContentLengthLong(res.getBytes().length);
                httpServletResponse.getWriter().write(res);
                return;
            } else {
                httpServletResponse.addHeader(CSP_HEADER, cspPolicy);
            }
        }
        chain.doFilter(request, response);
    }

    private boolean requestMatches(HttpServletRequest httpServletRequest) {
        return this.requestMatcher.matches(httpServletRequest);
    }

    private String generateRandomNonce(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
