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
package com.wavemaker.runtime.security;

/**
 * Created by arjuns on 13/2/17.
 */
public class SecurityConstants {

    private SecurityConstants(){}

    public static final String APPLICATION_JSON = "application/json";
    public static final String SESSION_NOT_FOUND = "Session Not Found";
    public static final String X_WM_LOGIN_ERROR_MESSAGE = "X-WM-Login-ErrorMessage";

    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String NO_CACHE = "no-cache";
    public static final String EXPIRES = "Expires";
    public static final String PRAGMA = "Pragma";
    public static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain;charset=utf-8";
}
