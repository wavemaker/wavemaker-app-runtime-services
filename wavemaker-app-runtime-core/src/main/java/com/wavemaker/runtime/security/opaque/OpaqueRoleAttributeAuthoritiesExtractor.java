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

package com.wavemaker.runtime.security.opaque;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.StringUtils;

public class OpaqueRoleAttributeAuthoritiesExtractor {
    public List<GrantedAuthority> getAuthorities(OAuth2AuthenticatedPrincipal authenticatedPrincipal, String roleAttributeName) {
        Object authorities = authenticatedPrincipal.getAttribute(roleAttributeName);
        List<GrantedAuthority> grantedAuthorities = getAuthorities(authorities).stream().
            map(authority -> new SimpleGrantedAuthority("ROLE_" + authority)).collect(Collectors.toList());
        return grantedAuthorities;
    }

    private Collection<String> getAuthorities(Object authorities) {
        if (authorities instanceof String) {
            if (StringUtils.hasText((String) authorities)) {
                return Arrays.asList(((String) authorities).split(" "));
            }
            return Collections.emptyList();
        }
        if (authorities instanceof Collection) {
            return (Collection<String>) authorities;
        }
        return Collections.emptyList();
    }
}