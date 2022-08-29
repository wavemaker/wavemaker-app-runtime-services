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
package com.wavemaker.runtime.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Uday Shankar
 */
public class SkipSupportedSecurityFilter extends GenericFilterBean {

    @Autowired(required = false)
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            if (("true".equals(((HttpServletRequest) servletRequest).getHeader("skipSecurity")))) {
                // Ignore the DelegatingProxyFilter delegate
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                // Call the delegate
                if (springSecurityFilterChain == null) {
                    throw new IllegalStateException();
                }
                springSecurityFilterChain.doFilter(servletRequest, servletResponse, filterChain);
            }
        } finally {
            SecurityContextHolder.clearContext(); //Cleaning any Thread local map values if created
        }
    }
}
