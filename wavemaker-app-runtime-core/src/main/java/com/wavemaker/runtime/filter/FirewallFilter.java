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

package com.wavemaker.runtime.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import com.wavemaker.runtime.data.util.UrlParserUtils;


/*
 * This filter wraps ServletRequest and ServletResponse objects with StrictHttpFirewall
 * FirewalledResponse validates and blocks any CRLF characters if added to response object.
 * */

public class FirewallFilter implements Filter {

    private StrictHttpFirewall firewall = new StrictHttpFirewall();

    private static final Logger logger = LoggerFactory.getLogger(FirewallFilter.class);

    @Value("${security.general.request.allowedHosts}")
    private String hosts;

    private List<String> allowedHosts;

    @PostConstruct
    private void init() {
        logger.info("Allowed hostnames configured are {}", hosts);
        List<String> sanitisedHosts = new ArrayList<>();
        if (hosts != null && !hosts.isEmpty()) {
            String hostNamesLower = StringUtils.lowerCase(hosts);
            allowedHosts = Arrays.asList(hostNamesLower.split(","));
            allowedHosts.stream().map(UrlParserUtils::trimUrlForHostName)
                .collect(Collectors.toCollection(() -> sanitisedHosts));
        }

        firewall.setAllowedHostnames(hostName -> {
            if (hosts != null && !hosts.isEmpty()) {
                return sanitisedHosts.contains(StringUtils.lowerCase(hostName));
            }
            return true;
        });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        FirewalledRequest firewalledRequest = firewall.getFirewalledRequest((HttpServletRequest) request);
        HttpServletResponse firewalledResponse = firewall.getFirewalledResponse((HttpServletResponse) response);

        filterChain.doFilter(firewalledRequest, firewalledResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}

