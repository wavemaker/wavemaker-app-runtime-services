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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.wavemaker.runtime.security.model.AuthProviderType;

public class LogoutSuccessHandlerRegistry {

    private final Map<AuthProviderType, LogoutSuccessHandler> providerTypeVsLogoutSuccessHandler = new ConcurrentHashMap<>();

    public void registerLogoutSuccessHandler(AuthProviderType authProviderType, LogoutSuccessHandler logoutSuccessHandler) {
        this.providerTypeVsLogoutSuccessHandler.put(authProviderType, logoutSuccessHandler);
    }

    public LogoutSuccessHandler getLogoutSuccessHandler(AuthProviderType authProviderType) {
        return providerTypeVsLogoutSuccessHandler.get(authProviderType);
    }
}
