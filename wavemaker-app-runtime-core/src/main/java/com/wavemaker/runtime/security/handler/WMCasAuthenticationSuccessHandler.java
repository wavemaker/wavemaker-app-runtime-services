package com.wavemaker.runtime.security.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAuthenticationToken;

import com.wavemaker.runtime.security.Attribute;
import com.wavemaker.runtime.security.WMAuthentication;

public class WMCasAuthenticationSuccessHandler implements WMAuthenticationSuccessHandler {

    private Logger logger = LoggerFactory.getLogger(WMCasAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, WMAuthentication authentication) {
        CasAuthenticationToken casAuthentication = (CasAuthenticationToken)authentication.getAuthenticationSource();
        Map<String, Object> attributes = casAuthentication.getAssertion().getPrincipal().getAttributes();
        logger.debug("Cas authentication user attributes : {}", attributes);
        attributes.forEach((key, value) -> {
            authentication.addAttribute(key, value, Attribute.AttributeScope.ALL);
        });
    }
}
