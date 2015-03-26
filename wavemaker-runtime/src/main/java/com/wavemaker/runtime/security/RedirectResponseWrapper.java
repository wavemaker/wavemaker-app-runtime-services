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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrappter which overrides and captures the redirect requests. This class is used by AcegiAjaxFilter.
 * 
 * @author Frankie Fu
 */
public class RedirectResponseWrapper extends HttpServletResponseWrapper {

    private String redirect;

    public RedirectResponseWrapper(HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
    }

    public String getRedirect() {
        return this.redirect;
    }

    @Override
    public void sendRedirect(String string) throws IOException {
        this.redirect = string;
    }
}
