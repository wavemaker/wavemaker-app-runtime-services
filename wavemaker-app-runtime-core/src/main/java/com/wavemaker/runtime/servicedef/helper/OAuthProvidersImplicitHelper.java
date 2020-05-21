package com.wavemaker.runtime.servicedef.helper;

import java.io.IOException;
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

public class OAuthProvidersImplicitHelper {

    public static final String OAUTH_PROVIDER_JSON_PATH = "/oauth-providers.json";
    public static final String OAUTH_PROVIDER = "oauthProvider";

    public static Map<String, Map<String, OAuth2ProviderConfig>> getOAuth2ProviderWithImplicitFlow() {
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = patternResolver.getResource(OAUTH_PROVIDER_JSON_PATH);
        if (resource.exists()) {
            Map<String, Map<String, OAuth2ProviderConfig>> oauthProvidersMap = new HashMap<>();
            try {
                String oauthProviders = WMIOUtils.toString(resource.getInputStream());
                if (oauthProviders != null) {
                    List<OAuth2ProviderConfig> oAuth2ProviderConfigs = JSONUtils.toObject(oauthProviders, new TypeReference<List<OAuth2ProviderConfig>>() {
                    });
                    Map<String, OAuth2ProviderConfig> oAuth2ProviderConfigMap = oAuth2ProviderConfigs.stream()
                            .filter(o -> o.getOauth2Flow() == OAuth2Flow.IMPLICIT)
                            .collect(Collectors.toMap(OAuth2ProviderConfig::getProviderId, o -> o));

                    oauthProvidersMap.put(OAUTH_PROVIDER, oAuth2ProviderConfigMap);
                    return oauthProvidersMap;
                }
            } catch (IOException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.oauth2provider.failure"), e, resource.getFilename());
            }
        }
        return null;
    }
}
