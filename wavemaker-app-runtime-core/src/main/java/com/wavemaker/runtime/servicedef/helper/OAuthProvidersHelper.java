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
package com.wavemaker.runtime.servicedef.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.auth.oauth2.OAuth2Flow;
import com.wavemaker.commons.auth.oauth2.OAuth2ProviderConfig;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.util.WMIOUtils;

public class OAuthProvidersHelper {

    public static final String OAUTH_PROVIDER_JSON_PATH = "/oauth-providers.json";
    public static final String OAUTH_PROVIDER = "oauthProvider";

    public static Map<String, Map<String, OAuth2ProviderConfig>> getOAuth2ProviderWithImplicitFlow() {
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = patternResolver.getResource(OAUTH_PROVIDER_JSON_PATH);
        try {
            if (resource.exists()) {
                return getOAuth2ProvidersMap(resource.getInputStream());
            }
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.oauth2provider.failure"), e, resource.getFilename());
        }
        return null;
    }

    public static Map<String, Map<String, OAuth2ProviderConfig>> getOAuth2ProvidersMap(InputStream inputStream) {
        Map<String, Map<String, OAuth2ProviderConfig>> oauthProvidersMap = new HashMap<>();
        String oauthProviders = WMIOUtils.toString(inputStream);
        if (oauthProviders != null) {
            List<OAuth2ProviderConfig> oAuth2ProviderConfigs = null;
            try {
                oAuth2ProviderConfigs = JSONUtils.toObject(oauthProviders, new TypeReference<List<OAuth2ProviderConfig>>() {
                });
            } catch (IOException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.oauth2provider.failure"), e);
            }
            oAuth2ProviderConfigs.forEach(provider -> provider.setClientSecret(null));
            Map<String, OAuth2ProviderConfig> oAuth2ProviderConfigMap = oAuth2ProviderConfigs.stream()
                    .filter(oAuth2ProviderConfig -> {
                        if (oAuth2ProviderConfig.getOauth2Flow() == OAuth2Flow.IMPLICIT) {
                            return true;
                        } else {
                            return oAuth2ProviderConfig.getoAuth2Pkce() != null &&
                                    oAuth2ProviderConfig.getoAuth2Pkce().isEnabled();
                        }
                    })
                    .collect(Collectors.toMap(OAuth2ProviderConfig::getProviderId, o -> o));

            oauthProvidersMap.put(OAUTH_PROVIDER, oAuth2ProviderConfigMap);
        }
        return oauthProvidersMap;
    }
}
