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

package com.wavemaker.runtime.security.enabled.configuration.requestmatcher;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class StatelessRequestMatcher implements RequestMatcher {

    private final String tokenParameter;

    public StatelessRequestMatcher(String tokenParameter) {
        this.tokenParameter = tokenParameter;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if ((authHeader != null && (authHeader.startsWith("Basic") || authHeader.startsWith("Bearer"))) ||
            StringUtils.isNotBlank(request.getHeader(tokenParameter))) {
            return true;
        } else if (!Objects.equals(request.getContentType(), MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            return StringUtils.isNotBlank(request.getParameter(tokenParameter));
        }
        return false;
    }
}

