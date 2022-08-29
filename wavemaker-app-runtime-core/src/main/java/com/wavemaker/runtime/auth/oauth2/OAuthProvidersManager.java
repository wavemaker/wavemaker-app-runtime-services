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
package com.wavemaker.runtime.auth.oauth2;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.auth.oauth2.OAuth2Flow;
import com.wavemaker.commons.auth.oauth2.OAuth2ProviderConfig;
import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.io.File;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.runtime.commons.util.PropertyPlaceHolderReplacementHelper;

public class OAuthProvidersManager {

    public static final String OAUTH_PROVIDER = "oauthProvider";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private PropertyResolver propertyResolver;

    private List<OAuth2ProviderConfig> oAuth2ProviderConfigList;
    private Map<String, Map<String, OAuth2ProviderConfig>> oAuth2ImplicitProviderMap;

    @PostConstruct
    private void init() {
        // NOTE: Make sure client secret is never passed to ui in any case
        oAuth2ProviderConfigList = constructOAuth2ProviderConfigList();
        oAuth2ImplicitProviderMap = getOAuth2ImplicitProviderMap(oAuth2ProviderConfigList);
    }

    public List<OAuth2ProviderConfig> getOAuth2ProviderConfigList() {
        return oAuth2ProviderConfigList;
    }

    public Map<String, Map<String, OAuth2ProviderConfig>> getOAuth2ProviderWithImplicitFlow() {
        return oAuth2ImplicitProviderMap;
    }

    private List<OAuth2ProviderConfig> constructOAuth2ProviderConfigList() {
        File classPathFile = new ClassPathFile(resourceLoader.getClassLoader(), "oauth-providers.json");
        if (classPathFile.exists()) {
            PropertyPlaceHolderReplacementHelper propertyPlaceHolderReplacementHelper = new PropertyPlaceHolderReplacementHelper();
            Reader reader = propertyPlaceHolderReplacementHelper.getPropertyReplaceReader(classPathFile, propertyResolver);
            return getOAuth2ProviderConfigList(reader);
        } else {
            return Collections.emptyList();
        }
    }

    public static Map<String, Map<String, OAuth2ProviderConfig>> getOAuth2ImplicitProviderMap(File file) {
        List<OAuth2ProviderConfig> oAuth2ProviderConfigList = getOAuth2ProviderConfigList(file.getContent().asReader());
        return getOAuth2ImplicitProviderMap(oAuth2ProviderConfigList);
    }

    private static List<OAuth2ProviderConfig> getOAuth2ProviderConfigList(Reader reader) {
        try {
            return JSONUtils.toObject(reader, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new WMRuntimeException(e);
        }
    }

    private static Map<String, Map<String, OAuth2ProviderConfig>> getOAuth2ImplicitProviderMap(List<OAuth2ProviderConfig> oAuth2ProviderConfigList) {
        Map<String, OAuth2ProviderConfig> oAuth2ProviderConfigMap = oAuth2ProviderConfigList.stream().filter(oAuth2ProviderConfig ->
                        (oAuth2ProviderConfig.getOauth2Flow() == OAuth2Flow.IMPLICIT) || (oAuth2ProviderConfig.getoAuth2Pkce() != null &&
                            oAuth2ProviderConfig.getoAuth2Pkce().isEnabled()))
                .collect(Collectors.toMap(OAuth2ProviderConfig::getProviderId, o -> o));
        Map<String, Map<String, OAuth2ProviderConfig>> oauthProvidersMap = new HashMap<>();
        oauthProvidersMap.put(OAUTH_PROVIDER, oAuth2ProviderConfigMap);
        return oauthProvidersMap;
    }
}