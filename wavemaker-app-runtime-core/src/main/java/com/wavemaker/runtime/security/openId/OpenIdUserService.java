/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.security.openId;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.CollectionUtils;

import com.wavemaker.runtime.security.core.AuthoritiesProvider;
import com.wavemaker.runtime.security.core.TenantIdProvider;

/**
 * Loads authorities associated with the authenticated user, using {@link AuthoritiesProvider} class.
 * <p>
 * Created by srujant on 8/8/18.
 */
public class OpenIdUserService extends OidcUserService {

    @Autowired(required = false)
    private AuthoritiesProvider authoritiesProvider;

    private TenantIdProvider tenantIdProvider;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        if (authoritiesProvider != null) {
            OpenIdAuthenticationContext openIdAuthenticationContext = new OpenIdAuthenticationContext(oidcUser.getName(), oidcUser);
            List<GrantedAuthority> grantedAuthorities = authoritiesProvider.loadAuthorities(openIdAuthenticationContext);
            if (!CollectionUtils.isEmpty(grantedAuthorities)) {
                String userNameAttributeName = userRequest.getClientRegistration()
                        .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
                if (org.springframework.util.StringUtils.hasText(userNameAttributeName)) {
                    oidcUser = new WMOidcUser(new HashSet<>(grantedAuthorities), oidcUser.getIdToken(), oidcUser.getUserInfo(), userNameAttributeName);
                } else {
                    oidcUser = new WMOidcUser(new HashSet<>(grantedAuthorities), userRequest.getIdToken(), oidcUser.getUserInfo());
                }
            }
        }

        if (tenantIdProvider != null) {
            WMOidcUser wmOidcUser = (WMOidcUser) oidcUser;
            OpenIdAuthenticationContext openIdAuthenticationContext = new OpenIdAuthenticationContext(oidcUser.getName(), oidcUser);
            Object tenantId = tenantIdProvider.loadTenantId(openIdAuthenticationContext);
            wmOidcUser.setTenantId(tenantId);
            return wmOidcUser;
        }
        return oidcUser;
    }

    public TenantIdProvider getTenantIdProvider() {
        return tenantIdProvider;
    }

    public void setTenantIdProvider(TenantIdProvider tenantIdProvider) {
        this.tenantIdProvider = tenantIdProvider;
    }
}
