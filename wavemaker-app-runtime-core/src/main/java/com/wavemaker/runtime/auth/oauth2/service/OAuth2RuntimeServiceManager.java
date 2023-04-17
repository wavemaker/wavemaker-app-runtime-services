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
package com.wavemaker.runtime.auth.oauth2.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.ResourceNotFoundException;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.auth.oauth2.OAuth2Constants;
import com.wavemaker.commons.auth.oauth2.OAuth2Helper;
import com.wavemaker.commons.auth.oauth2.OAuth2ProviderConfig;
import com.wavemaker.commons.auth.oauth2.extractors.AccessTokenRequestContext;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.app.AppFileSystem;
import com.wavemaker.runtime.auth.oauth2.OAuthProvidersManager;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.commons.WMObjectMapper;
import com.wavemaker.runtime.rest.builder.HttpRequestDetailsBuilder;
import com.wavemaker.runtime.rest.model.HttpRequestDetails;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;
import com.wavemaker.runtime.rest.service.RestConnector;

/**
 * Created by srujant on 18/7/17.
 */
public class OAuth2RuntimeServiceManager {

    private static final String REDIRECT_URL = "/services/oauth2/${providerId}/callback";
    private String customUrlScheme;

    @Autowired
    private OAuthProvidersManager oAuthProvidersManager;

    @Autowired
    private RestConnector restConnector;

    private static final Logger logger = LoggerFactory.getLogger(OAuth2RuntimeServiceManager.class);

    public String getAuthorizationUrl(String providerId, String requestSourceType, String key, HttpServletRequest httpServletRequest) {
        try {
            OAuth2ProviderConfig oAuth2ProviderConfig = getOAuthProviderConfig(providerId);
            String appPath = HttpRequestUtils.getApplicationBaseUrl(httpServletRequest);
            String redirectUrl = getRedirectUrl(providerId, appPath);
            Map<String, String> stateObject = new HashMap<>();
            stateObject.put("mode", "runtTime");
            stateObject.put("appPath", appPath);
            stateObject.put("key", key);
            stateObject.put(OAuth2Constants.REQUEST_SOURCE_TYPE, requestSourceType);
            String stateParameter = WMObjectMapper.getInstance().writeValueAsString(stateObject);
            return OAuth2Helper.getAuthorizationUrl(oAuth2ProviderConfig, redirectUrl, stateParameter);
        } catch (JsonProcessingException e) {
            throw new WMRuntimeException(e);
        }
    }

    private String getRedirectUrl(String providerId, String appPath) {
        String redirectUrl;
        String studioUrl = RuntimeEnvironment.getStudioUrl();
        if (StringUtils.isNotBlank(studioUrl)) {
            redirectUrl = studioUrl + REDIRECT_URL;
        } else {
            redirectUrl = new StringBuilder(appPath).append(REDIRECT_URL).toString();
        }
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("providerId", providerId);
        redirectUrl = StringSubstitutor.replace(redirectUrl, valuesMap);
        return redirectUrl;
    }

    public String callBack(String providerId, String redirectUrl, String code, String state, HttpServletRequest httpServletRequest) {
        OAuth2ProviderConfig oAuth2ProviderConfig = getOAuthProviderConfig(providerId);
        if (StringUtils.isBlank(redirectUrl)) {
            redirectUrl = new StringBuilder().append(HttpRequestUtils.getApplicationBaseUrl(httpServletRequest))
                .append("/services/oauth2/").append(providerId).append("/callback").toString();
        }

        String requestBody = OAuth2Helper.getAccessTokenApiRequestBody(oAuth2ProviderConfig, code, redirectUrl);

        HttpRequestDetails httpRequestDetails = HttpRequestDetailsBuilder.create(oAuth2ProviderConfig.getAccessTokenUrl())
            .setMethod("POST")
            .setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .setRequestBody(requestBody).build();

        HttpResponseDetails httpResponseDetails = restConnector.invokeRestCall(httpRequestDetails);

        if (httpResponseDetails.getStatusCode() == 200) {
            String response = WMIOUtils.toString(httpResponseDetails.getBody());
            AccessTokenRequestContext accessTokenRequestContext = new AccessTokenRequestContext(httpResponseDetails.getHeaders().getContentType(),
                oAuth2ProviderConfig.getAccessTokenUrl(), response);

            String accessToken = OAuth2Helper.extractAccessToken(accessTokenRequestContext);
            String requestSourceType = null;
//                TODO Have to perform encryption on accessToken
            if (state != null) {
                Map<String, String> jsonObject = OAuth2Helper.getStateObject(state);
                if (jsonObject.containsKey(OAuth2Constants.REQUEST_SOURCE_TYPE)) {
                    requestSourceType = jsonObject.get(OAuth2Constants.REQUEST_SOURCE_TYPE);
                }
                if ("MOBILE".equalsIgnoreCase(requestSourceType) && customUrlScheme == null) {
                    setCustomUrlScheme();
                }
            }
            return OAuth2Helper.getCallbackResponse(providerId, accessToken, customUrlScheme, requestSourceType);
        } else {
            logger.error("Failed to fetch access token, request made is {} and its response is {}", httpRequestDetails, httpResponseDetails);
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.failed.to.fetch.accessToken"));
        }
    }

    private OAuth2ProviderConfig getOAuthProviderConfig(String providerId) {
        for (OAuth2ProviderConfig oAuth2ProviderConfig : oAuthProvidersManager.getOAuth2ProviderConfigList()) {
            if (Objects.equals(oAuth2ProviderConfig.getProviderId(), providerId)) {
                return oAuth2ProviderConfig;
            }
        }
        throw new ResourceNotFoundException(MessageResource.create("com.wavemaker.runtime.OAuth2ProviderConfig.not.found"), providerId);
    }

    private synchronized void setCustomUrlScheme() {
        if (customUrlScheme == null) {
            InputStream inputStream = null;
            try {
                AppFileSystem appFileSystem = WMAppContext.getInstance().getSpringBean(AppFileSystem.class);
                inputStream = appFileSystem.getWebappResource("config.json");
                Map<String, String> configJsonObject = JSONUtils.toObject(inputStream, Map.class);
                customUrlScheme = configJsonObject.get(OAuth2Constants.CUSTOM_URL_SCHEME);
            } catch (IOException e) {
                throw new WMRuntimeException(e);
            } finally {
                WMIOUtils.closeSilently(inputStream);
            }
        }
    }

}
