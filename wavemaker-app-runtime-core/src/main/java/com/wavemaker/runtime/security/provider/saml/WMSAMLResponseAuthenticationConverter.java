package com.wavemaker.runtime.security.provider.saml;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.util.CollectionUtils;

import com.wavemaker.runtime.security.WMUser;
import com.wavemaker.runtime.security.provider.saml.util.SAMLUtils;

public class WMSAMLResponseAuthenticationConverter implements Converter<ResponseToken, Saml2Authentication> {

    private SAMLUserDetailsService samlUserDetailsService;

    @Override
    public Saml2Authentication convert(ResponseToken responseToken) {
        Response response = responseToken.getResponse();
        Saml2AuthenticationToken token = responseToken.getToken();
        Assertion assertion = CollectionUtils.firstElement(response.getAssertions());
        String username = assertion.getSubject().getNameID().getValue();
        Map<String, List<Object>> attributes = SAMLUtils.getAssertionAttributes(assertion);
        Collection<GrantedAuthority> authorities = samlUserDetailsService.getAuthorities(responseToken);
        WMUser wmUser = new WMUser("", username, "", username, 0, true, true, true, true, authorities, System.currentTimeMillis());
        Saml2Authentication saml2Authentication = new Saml2Authentication(new DefaultSaml2AuthenticatedPrincipal(username, attributes),
                token.getSaml2Response(),
                authorities);
        saml2Authentication.setDetails(wmUser);
        return saml2Authentication;
    }

    public void setSamlUserDetailsService(SAMLUserDetailsService samlUserDetailsService) {
        this.samlUserDetailsService = samlUserDetailsService;
    }

}
