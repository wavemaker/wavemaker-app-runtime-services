package com.wavemaker.runtime.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.StrictHttpFirewall;


/*
 * This filter wraps ServletRequest and ServletResponse objects with StrictHttpFirewall
 * FirewalledResponse validates and blocks any CRLF characters if added to response object.
 * */

public class FirewallFilter implements Filter {

    private StrictHttpFirewall firewall = new StrictHttpFirewall();

    @Value("${app.request.allowedHosts}")
    private String hosts;

    @PostConstruct
    private void init() {
        firewall.setAllowedHostnames(hostName -> {
            if (hosts != null && !hosts.isEmpty()) {
                List<String> allowedHosts = Arrays.asList(hosts.split(","));
                return allowedHosts.contains(hostName);
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

