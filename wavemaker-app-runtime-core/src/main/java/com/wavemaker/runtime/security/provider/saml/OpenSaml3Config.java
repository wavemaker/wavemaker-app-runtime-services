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

package com.wavemaker.runtime.security.provider.saml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml3AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml3LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml3LogoutResponseResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseResolver;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;
import com.wavemaker.runtime.security.provider.saml.logout.WMSaml2LogoutRequestResolver;

@Configuration
@Conditional(OpenSaml3VersionCondition.class)
public class OpenSaml3Config {

    @Value("${security.providers.saml.roleMappingEnabled:false}")
    private boolean roleMappingEnabled;

    @Value("${security.providers.saml.roleProvider:#{null}}")
    private String roleProvider;

    @Value("${security.providers.saml.roleAttributeName:#{null}}")
    private String roleAttributeName;

    @Autowired(required = false)
    private AuthoritiesProvider authoritiesProvider;

    @Bean("saml2AuthenticationRequestResolver")
    public WMSaml2AuthenticationRequestResolver saml2AuthenticationRequestResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new WMSaml2AuthenticationRequestResolver(relyingPartyRegistrationResolver,
            new OpenSaml3AuthenticationRequestResolver(relyingPartyRegistrationResolver));
    }

    @Bean("saml2LogoutRequestResolver")
    public WMSaml2LogoutRequestResolver saml2LogoutRequestResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new WMSaml2LogoutRequestResolver(relyingPartyRegistrationResolver,
            new OpenSaml3LogoutRequestResolver(relyingPartyRegistrationResolver));
    }

    @Bean("openSamlLogoutResponseResolver")
    public Saml2LogoutResponseResolver openSamlLogoutResponseResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new OpenSaml3LogoutResponseResolver(relyingPartyRegistrationResolver);
    }

    @Bean("samlAuthenticationProvider")
    public OpenSamlAuthenticationProvider samlAuthenticationProvider() {
        OpenSamlAuthenticationProvider openSamlAuthenticationProvider = new OpenSamlAuthenticationProvider();
        if (roleMappingEnabled && StringUtils.isNotBlank(roleProvider)) {
            if (roleProvider.equalsIgnoreCase("SAML") && StringUtils.isNotBlank(roleAttributeName)) {
                openSamlAuthenticationProvider.setResponseAuthenticationConverter(samlAttributesBasedAuthenticationConverter());
            } else if (roleProvider.equalsIgnoreCase("DATABASE")) {
                openSamlAuthenticationProvider.setResponseAuthenticationConverter(samlDatabaseBasedAuthenticationConverter());
            }
        }
        return openSamlAuthenticationProvider;
    }

    private Converter<OpenSamlAuthenticationProvider.ResponseToken, ? extends AbstractAuthenticationToken> samlDatabaseBasedAuthenticationConverter() {
        return (responseToken) -> {
            Saml2Authentication saml2Authentication = OpenSamlAuthenticationProvider.createDefaultResponseAuthenticationConverter().convert(responseToken);
            Object principal = saml2Authentication.getPrincipal();
            if (authoritiesProvider == null) {
                throw new WMRuntimeException("Authorities Provider is null, expecting DatabaseAuthorityProvider");
            }
            if (principal instanceof Saml2AuthenticatedPrincipal) {
                Saml2AuthenticatedPrincipal saml2Principal = (Saml2AuthenticatedPrincipal) principal;
                String username = saml2Principal.getName();
                List<GrantedAuthority> grantedAuthorities = new ArrayList<>(authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(username)));
                if (!saml2Authentication.getAuthorities().isEmpty()) {
                    grantedAuthorities.addAll(saml2Authentication.getAuthorities());
                }
                DefaultSaml2AuthenticatedPrincipal newSaml2Principal = new DefaultSaml2AuthenticatedPrincipal(saml2Principal.getName(), saml2Principal.getAttributes());
                newSaml2Principal.setRelyingPartyRegistrationId(saml2Principal.getRelyingPartyRegistrationId());
                return new Saml2Authentication(newSaml2Principal,
                    saml2Authentication.getSaml2Response(),
                    grantedAuthorities);
            }
            throw new WMRuntimeException("Error while creating Saml2Authentication, principal is not of type Saml2AuthenticatedPrincipal");
        };
    }

    private Converter<OpenSamlAuthenticationProvider.ResponseToken, ? extends AbstractAuthenticationToken> samlAttributesBasedAuthenticationConverter() {
        return (responseToken) -> {
            Saml2Authentication saml2Authentication = OpenSamlAuthenticationProvider.createDefaultResponseAuthenticationConverter().convert(responseToken);
            Object principal = saml2Authentication.getPrincipal();
            if (principal instanceof Saml2AuthenticatedPrincipal) {
                Saml2AuthenticatedPrincipal saml2Principal = (Saml2AuthenticatedPrincipal) principal;
                List<Object> attributes = saml2Principal.getAttributes().get(roleAttributeName);
                List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                attributes.forEach((attribute) -> grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + attribute.toString())));
                if (!saml2Authentication.getAuthorities().isEmpty()) {
                    grantedAuthorities.addAll(saml2Authentication.getAuthorities());
                }
                DefaultSaml2AuthenticatedPrincipal newSaml2Principal = new DefaultSaml2AuthenticatedPrincipal(saml2Principal.getName(), saml2Principal.getAttributes());
                newSaml2Principal.setRelyingPartyRegistrationId(saml2Principal.getRelyingPartyRegistrationId());
                return new Saml2Authentication(newSaml2Principal,
                    saml2Authentication.getSaml2Response(),
                    grantedAuthorities);
            }
            throw new WMRuntimeException("Error while creating Saml2Authentication, principal is not of type Saml2AuthenticatedPrincipal");
        };
    }

}
