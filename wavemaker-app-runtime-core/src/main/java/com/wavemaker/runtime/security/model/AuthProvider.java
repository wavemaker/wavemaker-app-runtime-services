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

package com.wavemaker.runtime.security.model;

import java.util.Objects;

public class AuthProvider {
    private final AuthProviderType authProviderType;
    private final String providerId;

    public AuthProvider(AuthProviderType authProviderType, String providerId) {
        this.authProviderType = authProviderType;
        this.providerId = providerId;
    }

    public AuthProviderType getAuthProviderType() {
        return authProviderType;
    }

    public String getProviderId() {
        return providerId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AuthProvider that = (AuthProvider) o;
        return authProviderType == that.authProviderType && Objects.equals(providerId, that.providerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authProviderType, providerId);
    }
}
