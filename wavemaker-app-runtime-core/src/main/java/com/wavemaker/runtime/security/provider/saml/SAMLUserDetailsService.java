package com.wavemaker.runtime.security.provider.saml;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider.ResponseToken;

public interface SAMLUserDetailsService {
    Collection<GrantedAuthority> getAuthorities(ResponseToken responseToken);
}
