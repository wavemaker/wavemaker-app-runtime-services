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
package com.wavemaker.runtime.webprocess.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.runtime.webprocess.WebProcessHelper;
import com.wavemaker.runtime.webprocess.model.WebProcess;

public abstract class WebProcessFilter extends GenericFilterBean {

    private String processName;

    public WebProcessFilter(String processName) {
        this.processName = processName;
    }

    @Override
    public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Cookie[] cookies = request.getCookies();
        Cookie webProcessCookie = WebProcessHelper.getCookie(cookies, WebProcessHelper.WEB_PROCESS_COOKIE_NAME);
        if (webProcessCookie != null) {
            WebProcess webProcess = WebProcessHelper.decodeWebProcess(webProcessCookie.getValue());
            if (webProcess.getProcessName().equals(this.processName)) {
                if (request.getRequestURI().endsWith("/services/webprocess/decode")
                    && this.onDecode(webProcess, request, response)) {
                    return;
                }
                String processOutput = this.endProcess(webProcess, request, response);
                if (processOutput != null) {
                    request.setAttribute(WebProcessHelper.WEB_PROCESS_OUTPUT, processOutput);
                    request.getRequestDispatcher("/services/webprocess/end").forward(servletRequest, servletResponse);
                    return;
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public abstract String endProcess(WebProcess webProcess, HttpServletRequest request, HttpServletResponse response) throws IOException;

    public boolean onDecode(WebProcess webProcess, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return false;
    }
}
