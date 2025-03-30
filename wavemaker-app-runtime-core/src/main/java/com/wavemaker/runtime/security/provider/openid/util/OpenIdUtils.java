/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.security.provider.openid.util;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.security.provider.openid.OpenIdConstants;

public class OpenIdUtils {

    public static String getRedirectUri(String clientId, String appPath) {
        String redirectUrl;
        String studioUrl = RuntimeEnvironment.getStudioUrl();
        if (StringUtils.isNotBlank(studioUrl)) {
            redirectUrl = studioUrl + OpenIdConstants.REDIRECT_URL;
        } else {
            redirectUrl = new StringBuilder(appPath).append(OpenIdConstants.REDIRECT_URL).toString();
        }
        Map<String, String> valuesMap = Collections.singletonMap(OpenIdConstants.REGISTRATION_ID_URI_VARIABLE_NAME, clientId);
        redirectUrl = StringSubstitutor.replace(redirectUrl, valuesMap);
        return redirectUrl;
    }
}
