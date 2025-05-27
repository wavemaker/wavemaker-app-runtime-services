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
package com.wavemaker.app.security.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * Created by ArjunSahasranam on 13/1/16.
 */
public class LoginConfig {
    @NotNull
    @ProfilizableProperty("${security.general.login.type}")
    private LoginType type;

    @ProfilizableProperty("${security.general.login.pageName}")
    private String pageName;

    @ProfilizableProperty(value = "${security.general.cookie.maxAge}")
    private int cookieMaxAge;

    @ProfilizableProperty("${security.general.cookie.path}")
    private String cookiePath;

    @ProfilizableProperty("${security.general.cookie.base64Encode:true}")
    private boolean cookieBase64Encode;

    @ProfilizableProperty("${security.general.cookie.jvmRoute}")
    private String jvmRoute;

    @ProfilizableProperty("${security.general.cookie.sameSite}")
    private String sameSite;

    @NotNull
    @Valid
    private SessionTimeoutConfig sessionTimeout;

    @NotNull
    @Valid
    private SessionConcurrencyConfig sessionConcurrencyConfig;

    public LoginType getType() {
        return type;
    }

    public void setType(final LoginType type) {
        this.type = type;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(final String pageName) {
        this.pageName = pageName;
    }

    public SessionTimeoutConfig getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(final SessionTimeoutConfig sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getCookieMaxAge() {
        return cookieMaxAge;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public boolean isCookieBase64Encode() {
        return cookieBase64Encode;
    }

    public void setCookieBase64Encode(boolean cookieBase64Encode) {
        this.cookieBase64Encode = cookieBase64Encode;
    }

    public String getJvmRoute() {
        return jvmRoute;
    }

    public void setJvmRoute(String jvmRoute) {
        this.jvmRoute = jvmRoute;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    public SessionConcurrencyConfig getSessionConcurrencyConfig() {
        return sessionConcurrencyConfig;
    }

    public void setSessionConcurrencyConfig(SessionConcurrencyConfig sessionConcurrencyConfig) {
        this.sessionConcurrencyConfig = sessionConcurrencyConfig;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LoginConfig that = (LoginConfig) o;

        return new EqualsBuilder()
            .append(type, that.type)
            .append(pageName, that.pageName)
            .append(cookieMaxAge, that.cookieMaxAge)
            .append(sessionTimeout, that.sessionTimeout)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(type)
            .append(pageName)
            .append(cookieMaxAge)
            .append(sessionTimeout)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "LoginConfig{" +
            "type=" + type +
            ", page='" + pageName + '\'' +
            ", cookieMaxAge=" + cookieMaxAge +
            ", sessionTimeout=" + sessionTimeout +
            '}';
    }
}
