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

package com.wavemaker.runtime.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.wavemaker.runtime.security.filter.WMRequestResponseHolderFilter;

public class WMSecurityUtils {

    public static void clearContext() {
        SecurityContextHolder.clearContext();
        saveContext();
    }

    public static void setContext(SecurityContext context) {
        SecurityContextHolder.setContext(context);
        saveContext();
    }

    public static void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        saveContext();
    }

    public static void setAuthentication(SecurityContext context, Authentication authentication) {
        context.setAuthentication(authentication);
        saveContext();
    }

    public static void saveContext() {
        saveContext(SecurityContextHolder.getContext());
    }

    public static void saveContext(SecurityContext securityContext) {
        HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        securityContextRepository.saveContext(securityContext, WMRequestResponseHolderFilter.getCurrentThreadHttpServletRequest(),
            WMRequestResponseHolderFilter.getCurrentThreadHttpServletResponse());
    }
}
