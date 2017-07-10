/**
 * Copyright © 2013 - 2017 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;

/**
 * Extends from the normal <code>ExceptionTranslationFilter</code>, this filter will send 403 if the request is a JSON
 * service request.
 * 
 * @author Frankie Fu
 */
public class JSONExceptionTranslationFilter extends ExceptionTranslationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JSONExceptionTranslationFilter.class);

    public JSONExceptionTranslationFilter(final AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationEntryPoint);
    }

    @Override
    protected void sendStartAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, AuthenticationException reason)
        throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestPath = httpRequest.getServletPath();
        if (requestPath != null && requestPath.endsWith(".json")) {
            logger.error("Access to {} is denied.  Reason: {}", requestPath, reason.getMessage());

            // send 403
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, requestPath);
        } else {
            // do normal processing
            super.sendStartAuthentication(request, response, chain, reason);
        }
    }

}
