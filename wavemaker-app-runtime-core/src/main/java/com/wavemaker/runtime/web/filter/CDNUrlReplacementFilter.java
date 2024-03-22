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
import java.io.PrintWriter;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.commons.proxy.AppPropertiesConstants;
import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.web.wrapper.CDNUrlReplacementServletResponseWrapper;

public class CDNUrlReplacementFilter extends GenericFilterBean {
    private static final String CDN_URL_PLACEHOLDER = "_cdnUrl_";

    private static final String DEFAULT_NG_BUILD_CDN_URL = "ng-bundle/";
    private static final String DEFAULT_WM_BUILD_CDN_URL = ".";
    private static final Logger cdnUrlReplacementFilterLogger = LoggerFactory.getLogger(CDNUrlReplacementFilter.class);

    private AntPathRequestMatcher indexPathMatcher = new AntPathRequestMatcher("/index.html");
    private AntPathRequestMatcher rootPathMatcher = new AntPathRequestMatcher("/");
    private String cdnUrl;
    @Value("${app.build.ui.mode}")
    private String buildMode;
    @Autowired
    private Environment environment;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        if (RuntimeEnvironment.isTestRunEnvironment()) {
            cdnUrl = getServletContext().getInitParameter("cdnUrl");
        } else if (Objects.equals(buildMode, "wm")) {
            cdnUrl = DEFAULT_WM_BUILD_CDN_URL;
        } else {
            cdnUrl = environment.getProperty(AppPropertiesConstants.APP_CDN_URL);
            if (StringUtils.isBlank(cdnUrl)) {
                cdnUrl = DEFAULT_NG_BUILD_CDN_URL;
            } else {
                if (!cdnUrl.endsWith("/")) {
                    cdnUrl = cdnUrl + "/";
                }
                if (!cdnUrl.endsWith(DEFAULT_NG_BUILD_CDN_URL)) {
                    cdnUrl = cdnUrl + DEFAULT_NG_BUILD_CDN_URL;
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        if (requestMatches(httpServletRequest)) {
            cdnUrlReplacementFilterLogger.debug("Replacing _cdnUrl_ placeholder with the value : {}", cdnUrl);
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            CDNUrlReplacementServletResponseWrapper cdnUrlReplacementServletResponseWrapper = new CDNUrlReplacementServletResponseWrapper(httpServletResponse);
            chain.doFilter(httpServletRequest, cdnUrlReplacementServletResponseWrapper);
            String response = new String(cdnUrlReplacementServletResponseWrapper.getByteArray());
            response = response.replace(CDN_URL_PLACEHOLDER, cdnUrl);
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