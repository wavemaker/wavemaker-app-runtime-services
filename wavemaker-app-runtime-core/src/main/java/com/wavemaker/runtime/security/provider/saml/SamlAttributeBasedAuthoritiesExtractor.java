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
package com.wavemaker.runtime.security.provider.saml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.wavemaker.runtime.security.provider.saml.util.SAMLUtils;

/**
 * Created by ArjunSahasranam on 23/11/16.
 */
public class SamlAttributeBasedAuthoritiesExtractor implements Converter<Assertion, Collection<? extends GrantedAuthority>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlAttributeBasedAuthoritiesExtractor.class);

    @Value("${security.providers.saml.roleAttributeName}")
    private String roleAttributeName;

    @Override
    public Collection<GrantedAuthority> convert(Assertion assertion) {
        List<GrantedAuthority> authorities = null;
        if (StringUtils.isNotBlank(roleAttributeName)) {
            Map<String, List<Object>> attributes = SAMLUtils.getAssertionAttributes(assertion);
            List<Object> attributeValues = attributes.get(roleAttributeName);
            LOGGER.info("Attribute values for {} is {}", roleAttributeName, attributeValues);
            authorities = new ArrayList<>(attributeValues.size());
            if (!attributes.isEmpty()) {
                for (Object attribute : attributeValues) {
                    authorities.add(new SimpleGrantedAuthority(attribute.toString()));
                }
            }
        }
        if (CollectionUtils.isEmpty(authorities)) {
            authorities = AuthorityUtils.NO_AUTHORITIES;
        }
        return authorities;
    }
}
