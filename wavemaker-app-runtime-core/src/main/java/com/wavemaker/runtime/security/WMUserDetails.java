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

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Seung Lee
 */
public interface WMUserDetails extends UserDetails {

    String getUserId();

    /**
     * Returns the user's long name.
     *
     * @return the user's long name (never <code>null</code>)
     */
    String getUserLongName();

    int getTenantId();

    long getLoginTime();

    Map<String, Object> getCustomAttributes();
}
