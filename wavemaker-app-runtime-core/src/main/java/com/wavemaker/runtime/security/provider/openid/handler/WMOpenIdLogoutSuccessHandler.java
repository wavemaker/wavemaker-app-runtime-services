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
package com.wavemaker.runtime.security.provider.openid.handler;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.CollectionUtils;

import com.wavemaker.runtime.security.provider.openid.OpenIdConstants;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.wrapper.StringWrapper;
import com.wavemaker.runtime.security.Attribute;
import com.wavemaker.runtime.security.WMAuthentication;
import com.wavemaker.runtime.security.provider.openid.OpenIdProviderRuntimeConfig;

public class WMOpenIdLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private Logger logger = LoggerFactory.getLogger(WMOpenIdLogoutSuccessHandler.class);

    @Autowired
    private OpenIdProviderRuntimeConfig openIdProviderRuntimeConfig;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private static final String QUESTION_MARK = "?";
    private static final String QUERY_PARAM_DELIMITER = "&";
    private static final String EQUALS = "=";
    private static final String QUERY_PARAM_ID_TOKEN_HINT = "id_token_hint";
    private static final String QUERY_PARAM_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
    private static final String URL_DELIMITER = "://";
    private static final String COLON = ":";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        if (targetUrl != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JSONUtils.toJSON(new StringWrapper(targetUrl)));
            response.getWriter().flush();
            return;
        }
        redirectStrategy.sendRedirect(request, response, super.determineTargetUrl(request, response));
    }

    /**
     * TODO Always taking the first openId providerInfo, find a better-way to get a particular ProviderInfo from the list
     **/
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String logoutUrl = null;
        if (openIdProviderRuntimeConfig != null && !CollectionUtils.isEmpty(openIdProviderRuntimeConfig.getOpenIdProviderInfoList())) {
            logoutUrl = openIdProviderRuntimeConfig.getOpenIdProviderInfoList().get(0).getLogoutUrl();
        }
        if (StringUtils.isNotBlank(logoutUrl) && authentication != null) {
            StringBuilder targetUrl = new StringBuilder()
                .append(logoutUrl).append(QUESTION_MARK)
                .append(postLogoutUrlQueryParam(request))
                .append(QUERY_PARAM_DELIMITER)
                .append(idTokenHintQueryParam(((WMAuthentication) authentication).getAttributes()));
            logger.info("Using the {} logoutUrl", targetUrl);
            return targetUrl.toString();
        }
        return null;
    }

    private String idTokenHintQueryParam(Map<String, Attribute> attributes) {
        return QUERY_PARAM_ID_TOKEN_HINT + EQUALS + attributes.get(OpenIdConstants.ID_TOKEN_VALUE).getValue();
    }

    private String postLogoutUrlQueryParam(HttpServletRequest request) {
        String postLogoutUrl = buildPlatformLogoutUrl(request);
        logger.info("Post logout url : {}", postLogoutUrl);
        return QUERY_PARAM_POST_LOGOUT_REDIRECT_URI + EQUALS + postLogoutUrl;
    }

    private String buildPlatformLogoutUrl(HttpServletRequest request) {
        StringBuilder postLogoutUrl = new StringBuilder();
        postLogoutUrl.append(request.getScheme()).append(URL_DELIMITER).append(request.getServerName());
        if (!((request.getScheme().equals(SCHEME_HTTP) && request.getServerPort() == DEFAULT_HTTP_PORT)
            || (request.getScheme().equals(SCHEME_HTTPS) && request.getServerPort() == DEFAULT_HTTPS_PORT))) {
            postLogoutUrl.append(COLON).append(request.getServerPort());
        }
        postLogoutUrl.append(request.getContextPath());
        return postLogoutUrl.toString();
    }
}
