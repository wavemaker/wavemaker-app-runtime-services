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
package com.wavemaker.runtime.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wavemaker.commons.util.EncodeUtils;
import com.wavemaker.runtime.security.authenticationToken.AbstractAuthenticationToken;
import com.wavemaker.runtime.security.authority.SimpleGrantedAuthority;

/**
 * Created by srujant on 13/8/18.
 */
public class WMAuthentication extends AbstractAuthenticationToken {

    private static String prefix = "ROLE_";

    private Map<String, Attribute> attributes = new HashMap<>();
    private String principal;
    private long loginTime;
    private String userId;

    @JsonIgnore
    private transient Authentication authenticationSource;

    public WMAuthentication() {
    }

    public WMAuthentication(Authentication authenticationSource) {
        super();
        setAuthorities(mapAuthorities(authenticationSource.getAuthorities()));
        this.principal = authenticationSource.getName();
        this.authenticationSource = authenticationSource;
        if (authenticationSource.getPrincipal() instanceof WMUser) {
            WMUser wmUser = (WMUser) authenticationSource.getPrincipal();
            this.userId = wmUser.getUserId();
            Map<String, Object> customAttributes = wmUser.getCustomAttributes();
            for (Map.Entry<String, Object> entry : customAttributes.entrySet()) {
                addAttribute(entry.getKey(), entry.getValue(), Attribute.AttributeScope.ALL);
            }
        }
        setAuthenticated(true);
        this.loginTime = System.currentTimeMillis();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public Authentication getAuthenticationSource() {
        return authenticationSource;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public void addAttribute(String key, Object value, Attribute.AttributeScope scope) {
        attributes.put(key, new Attribute(scope, value));
    }

    private Set<SimpleGrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        HashSet<SimpleGrantedAuthority> mapped = new HashSet<>(authorities.size());
        for (GrantedAuthority authority : authorities) {
            mapped.add(mapAuthority(authority.getAuthority()));
        }
        return mapped;
    }

    private SimpleGrantedAuthority mapAuthority(String authorityName) {
        if (prefix.length() > 0 && !authorityName.startsWith(prefix)) {
            authorityName = prefix + authorityName;
        }
        return new SimpleGrantedAuthority(EncodeUtils.encode(authorityName));
    }
}
