package com.wavemaker.runtime.security.provider.saml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;

import static com.wavemaker.runtime.security.provider.saml.SAMLHttpServletRequestWrapper.EndpointType.SSO;

public class WMSaml2WebSsoAuthenticationFilter extends Saml2WebSsoAuthenticationFilter {

    @Autowired
    private SAMLConfig samlConfig;

    @Autowired
    private Saml2AuthenticationTokenConverter saml2AuthenticationTokenConverter;

    public WMSaml2WebSsoAuthenticationFilter(AuthenticationConverter authenticationConverter) {
        super(authenticationConverter, Saml2WebSsoAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Saml2AuthenticationToken saml2AuthenticationToken = saml2AuthenticationTokenConverter.convert(request);
        if (SAMLConfig.ValidateType.RELAXED == samlConfig.getValidateType()) {
            SAMLHttpServletRequestWrapper requestWrapper = new SAMLHttpServletRequestWrapper(request, saml2AuthenticationToken, SSO);
            return super.attemptAuthentication(requestWrapper, response);
        } else {
            return super.attemptAuthentication(request, response);
        }
    }
}
