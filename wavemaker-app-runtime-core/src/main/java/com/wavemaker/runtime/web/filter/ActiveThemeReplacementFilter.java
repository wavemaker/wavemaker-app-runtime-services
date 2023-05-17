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

package com.wavemaker.runtime.web.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.web.wrapper.ActiveThemeReplacementServletResponseWrapper;

public class ActiveThemeReplacementFilter extends GenericFilterBean {
    private static final String ACTIVE_THEME_PLACEHOLDER = "_activeTheme_";

    private static final Logger logger = LoggerFactory.getLogger(ActiveThemeReplacementFilter.class);

    private AntPathRequestMatcher indexPathMatcher = new AntPathRequestMatcher("/index.html");
    private AntPathRequestMatcher rootPathMatcher = new AntPathRequestMatcher("/");

    private String activeTheme;

    @PostConstruct
    public void init() {
        InputStream resourceAsStream = getServletContext().getResourceAsStream("themes/themes-config.json");
        if (resourceAsStream != null) {
            try {
                activeTheme = new ObjectMapper().readTree(resourceAsStream).get("activeTheme").asText();
            } catch (IOException e) {
                logger.error("Error while reading themes-config.json file", e);
                throw new WMRuntimeException(e);
            }
            logger.info("Detected active theme as : {}", activeTheme);
        } else {
            logger.warn("themes-config.json file not found in classpath");
            throw new WMRuntimeException("themes-config.json file not found in classpath");
        }
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        if (requestMatches(httpServletRequest)) {
            logger.debug("Replacing _activeTheme_ placeholder with the value : {}", activeTheme);
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            ActiveThemeReplacementServletResponseWrapper activeThemeReplacementServletResponseWrapper =
                new ActiveThemeReplacementServletResponseWrapper(httpServletResponse);
            chain.doFilter(httpServletRequest, activeThemeReplacementServletResponseWrapper);
            String response = new String(activeThemeReplacementServletResponseWrapper.getByteArray());
            response = response.replace(ACTIVE_THEME_PLACEHOLDER, activeTheme);
            httpServletResponse.setContentLengthLong(response.getBytes().length);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(response);
            writer.flush();
            return;
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    protected boolean requestMatches(HttpServletRequest httpServletRequest) {
        return this.indexPathMatcher.matches(httpServletRequest) || this.rootPathMatcher.matches(httpServletRequest);
    }
}
