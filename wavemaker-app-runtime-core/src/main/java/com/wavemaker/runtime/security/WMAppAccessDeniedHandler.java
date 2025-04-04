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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.core.web.rest.ErrorResponse;
import com.wavemaker.commons.core.web.rest.ErrorResponses;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.runtime.commons.WMObjectMapper;

import static com.wavemaker.runtime.security.SecurityConstants.CACHE_CONTROL;
import static com.wavemaker.runtime.security.SecurityConstants.EXPIRES;
import static com.wavemaker.runtime.security.SecurityConstants.NO_CACHE;
import static com.wavemaker.runtime.security.SecurityConstants.PRAGMA;
import static com.wavemaker.runtime.security.SecurityConstants.TEXT_PLAIN_CHARSET_UTF_8;

/**
 * Created by kishorer on 4/7/16.
 */
public class WMAppAccessDeniedHandler extends AccessDeniedHandlerImpl {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException, ServletException {
        if (HttpRequestUtils.isAjaxRequest(request)) {
            String exceptionMessage = accessDeniedException.getMessage();
            Map<String, Object> errorMap = new HashMap(1);
            ErrorResponse errorResponse = new ErrorResponse();
            MessageResource messageResource = MessageResource.ACCESS_DENIED;
            errorResponse.setMessageKey(messageResource.getMessageKey());
            errorResponse.setMessage(messageResource.getMessageWithPlaceholders());
            errorResponse.setParameters(Arrays.asList(exceptionMessage));
            List<ErrorResponse> errorResponseList = new ArrayList<>(1);
            errorResponseList.add(errorResponse);
            errorMap.put("errors", new ErrorResponses(errorResponseList));
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader(CACHE_CONTROL, NO_CACHE);
            response.setDateHeader(EXPIRES, 0);
            response.setHeader(PRAGMA, NO_CACHE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
            PrintWriter writer = response.getWriter();
            writer.write(WMObjectMapper.getInstance().writeValueAsString(errorMap));
            writer.flush();
        } else {
            super.handle(request, response, accessDeniedException);
        }
    }
}
