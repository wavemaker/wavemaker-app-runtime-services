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
package com.wavemaker.runtime.security.csrf;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.wavemaker.app.security.models.CSRFConfig;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.security.AbstractLogoutHandler;

/**
 * Created by kishorer on 13/7/16.
 */
public class WMCsrfLogoutHandler extends AbstractLogoutHandler {

    @Value("${security.general.cookie.path}")
    private String cookiePath;

    public WMCsrfLogoutHandler(LogoutHandler logoutHandler) {
        super(logoutHandler);
    }

    @Override
    protected void postLogout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        CSRFConfig csrfConfig = WMAppContext.getInstance().getSpringBean("csrfConfig");
        if (csrfConfig.isEnforceCsrfSecurity()) {
            Cookie cookie = new Cookie(csrfConfig.getCookieName(), null);
            cookie.setMaxAge(0);
            String cookiePath;
            String contextPath = request.getContextPath();
            if (StringUtils.isNotBlank(this.cookiePath)) {
                cookiePath = this.cookiePath;
            } else if (StringUtils.isNotBlank(contextPath)) {
                cookiePath = contextPath;
            } else {
                cookiePath = "/";
            }
            cookie.setPath(cookiePath);
            cookie.setSecure(request.isSecure());
            response.addCookie(cookie);
        }
    }
}
