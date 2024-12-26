/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.security.handler.logout;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.wavemaker.runtime.security.WMAuthentication;

public class WMApplicationLogoutSuccessHandler implements LogoutSuccessHandler {

    private Map<String, LogoutSuccessHandler> providerTypeVsLogoutSuccessHandler = new ConcurrentHashMap<>();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String providerType = ((WMAuthentication) authentication).getProviderType();
        this.providerTypeVsLogoutSuccessHandler.get(providerType).onLogoutSuccess(request, response, authentication);
    }

    public void registerLogoutSuccessHandler(String providerType, LogoutSuccessHandler logoutSuccessHandler) {
        this.providerTypeVsLogoutSuccessHandler.put(providerType, logoutSuccessHandler);
    }
}
