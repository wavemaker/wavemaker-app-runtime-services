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

package com.wavemaker.runtime.security.provider.saml.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.wavemaker.commons.util.HttpRequestUtils;

public class SamlUtils {

    private static final Logger logger = LoggerFactory.getLogger(SamlUtils.class);

    public static String resolveRelayState(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        String appUrl = HttpRequestUtils.getApplicationBaseUrl(request);
        logger.debug("application baseUri is {}", appUrl);
        String redirectPage = request.getParameter("redirectPage");
        if (StringUtils.isNotEmpty(redirectPage) && StringUtils.isNotEmpty(appUrl) && !StringUtils
            .containsAny(appUrl, '#', '?')) {
            appUrl = appUrl + "#" + redirectPage;
        }
        return appUrl;
    }

}
