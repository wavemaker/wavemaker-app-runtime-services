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
package com.wavemaker.runtime.security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.wavemaker.runtime.security.WMAuthentication;

/**
 * Created by srujant on 23/11/18.
 */
public class WMSamlAuthenticationSuccessRedirectionHandler extends SavedRequestAwareAuthenticationSuccessHandler implements WMAuthenticationRedirectionHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, WMAuthentication wmAuthentication) throws IOException, ServletException {
        Authentication authentication = wmAuthentication.getAuthenticationSource();
        super.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);
    }
}
