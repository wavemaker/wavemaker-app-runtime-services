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
package com.wavemaker.runtime.webprocess.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.runtime.webprocess.WebProcessHelper;

public class LoginProcessFilter extends WebProcessFilter {

    public LoginProcessFilter() {
        super("LOGIN");
    }

    @Override
    public String endProcess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx != null)  {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Map<String,String> map = new HashMap<>();
                for (Cookie c : request.getCookies()) {
                    if (c.getValue() != null
                            && !WebProcessHelper.WEB_PROCESS_COOKIE_NAME.equalsIgnoreCase(c.getName())) {
                        map.put(c.getName(), c.getValue());
                    }
                }
                return JSONUtils.toJSON(map, false);
            }
        }
        return null;
    }
}
