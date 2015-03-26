/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Filter to support Acegi's Ajax based logout.
 * 
 * @author Frankie Fu
 */
public class AcegiAjaxLogoutFilter extends LogoutFilter {

    private static final String ACEGI_AJAX_LOGOUT_REQUEST_PARAM = "acegiAjaxLogout";

    private static final String SUCCESS_URL = "url";

    public AcegiAjaxLogoutFilter(String logoutSuccessUrl, LogoutHandler[] handlers) {
        super(logoutSuccessUrl, handlers);
    }

    //@Override
    protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        if (isAjaxRequest(request)) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = request.getContextPath() + url;
            }
            String jsonContent = "{\"" + SUCCESS_URL + "\":\"" + url + "\"}";
            response.getOutputStream().write(jsonContent.getBytes());
        } else {
        	throw new IOException("Hey, this shouldn't happen");
           // super.sendRedirect(request, response, url);
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return request.getParameter(ACEGI_AJAX_LOGOUT_REQUEST_PARAM) != null;
    }
}
