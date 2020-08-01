/**
 * Copyright © 2013 - 2017 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.header.writers.XContentTypeOptionsHeaderWriter;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filter implementation to add header X-Content-Type-Options.
 */
public class WMXContentTypeOptionsFilter extends GenericFilterBean {

    private XContentTypeOptionsHeaderWriter xContentTypeOptionsHeaderWriter;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();

        xContentTypeOptionsHeaderWriter = new XContentTypeOptionsHeaderWriter();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        xContentTypeOptionsHeaderWriter.writeHeaders(httpServletRequest, httpServletResponse);
        chain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    public void destroy() {
    }
}
