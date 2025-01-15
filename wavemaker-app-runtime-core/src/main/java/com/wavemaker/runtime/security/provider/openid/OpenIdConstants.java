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
package com.wavemaker.runtime.security.provider.openid;

/**
 * Created by srujant on 6/8/18.
 */
public class OpenIdConstants {

    public static final String REDIRECT_URL = "/oauth2/code/${registrationId}";
    public static final String REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId";
    public static final String APP_PATH = "appPath";

    public static final String RESPONSE_TYPE = "response_type";

    public static final String CLIENT_ID = "client_id";

    public static final String REDIRECT_URI = "redirect_uri";

    public static final String SCOPE = "scope";

    public static final String STATE = "state";

    public static final String CODE = "code";

    public static final String ID_TOKEN_VALUE = "idTokenValue";

    public static final String ACCESS_TOKEN_VALUE = "accessTokenValue";

    public static final String ERROR = "error";

    public static final String ERROR_DESCRIPTION = "error_description";

    public static final String ERROR_URI = "error_uri";

    public static final String REGISTRATION_ID = "registration_id";

    public static final String REDIRECT_PAGE = "redirectPage";

    public static final String PROVIDER_ID = "providerId";
    public static final String OPEN_ID_SCOPE = "openid";
}
