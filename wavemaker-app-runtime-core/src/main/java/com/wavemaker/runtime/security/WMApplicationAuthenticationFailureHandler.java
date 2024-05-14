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
package com.wavemaker.runtime.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.core.web.rest.ErrorResponse;
import com.wavemaker.commons.core.web.rest.ErrorResponses;
import com.wavemaker.runtime.commons.WMObjectMapper;
import com.wavemaker.runtime.security.handler.WMAuthenticationFailureHandler;

import static com.wavemaker.runtime.security.SecurityConstants.APPLICATION_JSON;
import static com.wavemaker.runtime.security.SecurityConstants.X_WM_LOGIN_ERROR_MESSAGE;

/**
 * @author Uday Shankar
 */
public class WMApplicationAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(WMApplicationAuthenticationFailureHandler.class);
    @Autowired(required = false)
    private List<WMAuthenticationFailureHandler> customFailureHandlerList = new ArrayList<>();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        invokeCustomWMAuthenticationFailureHandler(request, response, exception);
        if (!response.isCommitted()) {
            String exceptionMessage = exception.getMessage();
            Throwable rootCause = ExceptionUtils.getRootCause(exception.getCause());
            StringBuilder msg = new StringBuilder("Authentication Failed");
            if (rootCause instanceof WMRuntimeException) {
                msg.append(":").append(rootCause.getMessage());
            }
            response.setHeader(X_WM_LOGIN_ERROR_MESSAGE, msg.toString());
            Map<String, Object> errorMap = new HashMap(1);
            ErrorResponse errorResponse = new ErrorResponse();
            MessageResource messageResource = MessageResource.create("com.wavemaker.runtime.security.authentication.failed");
            errorResponse.setMessageKey(messageResource.getMessageKey());
            errorResponse.setMessage(messageResource.getMessageWithPlaceholders());
            errorResponse.setParameters(Arrays.asList(exceptionMessage));
            List<ErrorResponse> errorResponseList = new ArrayList<>(1);
            errorResponseList.add(errorResponse);
            errorMap.put("errors", new ErrorResponses(errorResponseList));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(APPLICATION_JSON);
            response.getWriter().write(WMObjectMapper.getInstance().writeValueAsString(errorMap));
        }
    }

    private void invokeCustomWMAuthenticationFailureHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (CollectionUtils.isNotEmpty(customFailureHandlerList)) {
            logger.info("Invoking CustomAuthenticationFailureHandlers");
            for (WMAuthenticationFailureHandler authenticationFailureHandler : customFailureHandlerList) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
            }
        }
    }
}

