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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

/**
 * Created by ArjunSahasranam on 18/1/16.
 */
public class SessionTimeoutConfig {
    @NotNull
    @ProfilizableProperty("${security.general.login.sessionTimeoutType}")
    private LoginType type;

    @ProfilizableProperty("${security.general.login.sessionTimeoutPageName}")
    private String pageName;

    @Min(1)
    @ProfilizableProperty("${security.general.session.timeout}")
    private int timeoutValue;

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

    public int getTimeoutValue() {
        return timeoutValue;
    }

    public void setTimeoutValue(final int timeoutValue) {
        this.timeoutValue = timeoutValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SessionTimeoutConfig that = (SessionTimeoutConfig) o;

        return new EqualsBuilder()
            .append(timeoutValue, that.timeoutValue)
            .append(type, that.type)
            .append(pageName, that.pageName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(type)
            .append(pageName)
            .append(timeoutValue)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "SessionTimeoutConfig{" +
            "type=" + type +
            ", pageName='" + pageName + '\'' +
            ", timeoutValue=" + timeoutValue +
            '}';
    }
}
