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
package com.wavemaker.runtime.security.csrf;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.util.Assert;

import com.wavemaker.commons.model.security.CSRFConfig;

/**
 * Created by srujant on 22/10/18.
 */
public class WMHttpSessionCsrfTokenRepository implements CsrfTokenRepository {


    private static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";

    private static final String DEFAULT_CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class
            .getName().concat(".CSRF_TOKEN");

    private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;

    private String headerName = DEFAULT_CSRF_HEADER_NAME;

    private String sessionAttributeName = DEFAULT_CSRF_TOKEN_ATTR_NAME;

    private CSRFConfig csrfConfig;

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request,
                          HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (token == null) {
                session.removeAttribute(this.sessionAttributeName);
            } else {
                session.setAttribute(this.sessionAttributeName, token);
            }
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (CsrfToken) session.getAttribute(this.sessionAttributeName);

    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(this.headerName, this.parameterName,
                createNewToken());
    }

    public void setParameterName(String parameterName) {
        if (StringUtils.isNotBlank(parameterName)) {
            this.parameterName = parameterName;
        }
    }

    public void setHeaderName(String headerName) {
        if (StringUtils.isNotBlank(headerName)) {
            this.headerName = headerName;
        }
    }

    public void setSessionAttributeName(String sessionAttributeName) {
        Assert.hasLength(sessionAttributeName,
                "sessionAttributename cannot be null or empty");
        this.sessionAttributeName = sessionAttributeName;
    }

    private String createNewToken() {
        return UUID.randomUUID().toString();
    }

    public void setCsrfConfig(CSRFConfig csrfConfig) {
        this.csrfConfig = csrfConfig;
        if (csrfConfig != null) {
            setHeaderName(csrfConfig.getHeaderName());
        }
    }
}
