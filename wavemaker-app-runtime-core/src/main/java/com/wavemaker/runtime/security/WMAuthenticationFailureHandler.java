/**
 * Copyright © 2015 WaveMaker, Inc.
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
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wavemaker.runtime.WMObjectMapper;
import com.wavemaker.studio.common.core.web.rest.ErrorResponse;
import com.wavemaker.studio.common.core.web.rest.ErrorResponses;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * @author Uday Shankar
 */
public class WMAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String exceptionMessage = exception.getMessage();
        String msg = "Authentication Failed: " + exceptionMessage;
        response.setHeader("X-WM-Login-ErrorMessage", msg);
        Map<String, Object> errorMap = new HashMap(1);
        ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setMessageKey("AUTHENTICATION_FAILED");
		errorResponse.setParameters(Arrays.asList(exceptionMessage));
        List<ErrorResponse> errorResponseList = new ArrayList<>(1);
        errorResponseList.add(errorResponse);
        errorMap.put("errors", new ErrorResponses(errorResponseList));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(WMObjectMapper.getInstance().writeValueAsString(errorMap));
    }
}

