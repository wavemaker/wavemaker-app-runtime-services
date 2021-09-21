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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.model.security.CSRFConfig;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.WMAppContext;
import com.wavemaker.runtime.security.model.LoginSuccessResponse;

import static com.wavemaker.runtime.security.SecurityConstants.*;

/**
 * Created by srujant on 19/11/18.
 */
public class WMCsrfTokenResponseWriterAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private CsrfTokenRepository csrfTokenRepository;

    public WMCsrfTokenResponseWriterAuthenticationSuccessHandler(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Optional<CsrfToken> csrfTokenOptional = getCsrfToken(request);
        if (HttpRequestUtils.isAjaxRequest(request)) {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
            response.setHeader(CACHE_CONTROL, NO_CACHE);
            response.setDateHeader(EXPIRES, 0);
            response.setHeader(PRAGMA, NO_CACHE);
            response.setStatus(HttpServletResponse.SC_OK);
            writeCsrfTokenToResponse(csrfTokenOptional, response);
            response.getWriter().flush();
        }
    }

    private void writeCsrfTokenToResponse(Optional<CsrfToken> csrfTokenOptional, HttpServletResponse response) throws IOException {
        if (csrfTokenOptional.isPresent()) {
            CsrfToken csrfToken = csrfTokenOptional.get();
            PrintWriter writer = response.getWriter();
            LoginSuccessResponse loginSuccessResponse = new LoginSuccessResponse();
            loginSuccessResponse.setWmCsrfToken(csrfToken.getToken());
            writer.println(JSONUtils.toJSON(loginSuccessResponse));
            writer.flush();
        }
    }

    public void setCsrfTokenRepository(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }


    private Optional<CsrfToken> getCsrfToken(HttpServletRequest request) {
        CSRFConfig csrfConfig = WMAppContext.getInstance().getSpringBean(CSRFConfig.class);
        if (csrfConfig != null && csrfConfig.isEnforceCsrfSecurity()) {
            CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
            return Optional.ofNullable(csrfToken);
        }
        return Optional.empty();
    }
}
