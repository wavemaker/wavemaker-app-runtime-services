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
package com.wavemaker.runtime.security.provider.openid;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wavemaker.commons.auth.oauth2.OAuth2Constants;

/**
 * Created by srujant on 7/8/18.
 */
public class InMemoryOpenIDAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Cache<String, OAuth2AuthorizationRequest> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(20).
        build();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = getStateParam(request);
        return cache.getIfPresent(state);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        String state = authorizationRequest.getState();
        if (state != null) {
            cache.put(state, authorizationRequest);
        }
    }

    private String getStateParam(HttpServletRequest request) {
        return request.getParameter(OAuth2Constants.STATE);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            cache.invalidate(authorizationRequest);
        }
        return authorizationRequest;
    }
}
