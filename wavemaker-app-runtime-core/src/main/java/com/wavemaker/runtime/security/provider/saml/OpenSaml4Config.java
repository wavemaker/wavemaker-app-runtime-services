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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutResponseResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseResolver;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.DefaultAuthenticationContext;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.provider.saml.logout.WMSaml2LogoutRequestResolver;
import com.wavemaker.runtime.security.provider.saml.util.SamlUtils;

@Configuration
@Conditional({SecurityEnabledCondition.class, SAMLSecurityProviderCondition.class, OpenSamlLatestVersionCondition.class})
public class OpenSaml4Config {

    @Value("${security.providers.saml.roleMappingEnabled:false}")
    private boolean roleMappingEnabled;

    @Value("${security.providers.saml.roleProvider:#{null}}")
    private String roleProvider;

    @Value("${security.providers.saml.roleAttributeName:#{null}}")
    private String roleAttributeName;

    @Autowired(required = false)
    private AuthoritiesProvider authoritiesProvider;

    @Bean(name = "relyingPartyRegistrationResolver")
    public RelyingPartyRegistrationResolverFactoryBean relyingPartyRegistrationResolver() {
        return new RelyingPartyRegistrationResolverFactoryBean();
    }

    @Bean("saml2AuthenticationRequestResolver")
    public Saml2AuthenticationRequestResolver saml2AuthenticationRequestResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        OpenSaml4AuthenticationRequestResolver openSaml4AuthenticationRequestResolver = new OpenSaml4AuthenticationRequestResolver(relyingPartyRegistrationResolver);
        openSaml4AuthenticationRequestResolver.setRelayStateResolver(SamlUtils::resolveRelayState);
        return openSaml4AuthenticationRequestResolver;
    }

    @Bean("saml2LogoutRequestResolver")
    public Saml2LogoutRequestResolver saml2LogoutRequestResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new WMSaml2LogoutRequestResolver(relyingPartyRegistrationResolver,
            new OpenSaml4LogoutRequestResolver(relyingPartyRegistrationResolver));
    }

    @Bean("openSamlLogoutResponseResolver")
    public Saml2LogoutResponseResolver openSamlLogoutResponseResolver(RelyingPartyRegistrationResolver relyingPartyRegistrationResolver) {
        return new OpenSaml4LogoutResponseResolver(relyingPartyRegistrationResolver);
    }

    @Bean("samlAuthenticationProvider")
    public AuthenticationProvider samlAuthenticationProvider() {
        OpenSaml4AuthenticationProvider openSaml4AuthenticationProvider = new OpenSaml4AuthenticationProvider();
        openSaml4AuthenticationProvider.setResponseAuthenticationConverter(customAuthenticationConverter());
        return openSaml4AuthenticationProvider;
    }

    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, ? extends AbstractAuthenticationToken> customAuthenticationConverter() {
        return responseToken -> {
            Saml2Authentication saml2Authentication = OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter().convert(responseToken);
            Object principal = saml2Authentication.getPrincipal();
            if (principal instanceof Saml2AuthenticatedPrincipal) {
                Saml2AuthenticatedPrincipal saml2Principal = (Saml2AuthenticatedPrincipal) principal;
                List<GrantedAuthority> grantedAuthorities = resolveGrantedAuthorities(saml2Principal);
                DefaultSaml2AuthenticatedPrincipal newSaml2Principal = new DefaultSaml2AuthenticatedPrincipal(saml2Principal.getName(), saml2Principal.getAttributes());
                newSaml2Principal.setRelyingPartyRegistrationId(saml2Principal.getRelyingPartyRegistrationId());
                return new Saml2Authentication(newSaml2Principal,
                    saml2Authentication.getSaml2Response(),
                    grantedAuthorities);
            }
            throw new WMRuntimeException("Error while creating Saml2Authentication, principal is not of type Saml2AuthenticatedPrincipal");
        };
    }

    private List<GrantedAuthority> resolveGrantedAuthorities(Saml2AuthenticatedPrincipal saml2Principal) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (roleMappingEnabled && StringUtils.isNotBlank(roleProvider)) {
            if (authoritiesProvider != null) {
                grantedAuthorities.addAll(authoritiesProvider.loadAuthorities(new DefaultAuthenticationContext(saml2Principal.getName())));
            } else {
                // roles are from saml user attributes
                List<Object> attributes = saml2Principal.getAttributes().get(roleAttributeName);
                for (Object attribute : attributes) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + attribute.toString()));
                }
            }
        }
        return grantedAuthorities;
    }

}
