package com.wavemaker.runtime.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;


/*
* This filter wraps ServletRequest and ServletResponse objects with StrictHttpFirewall
* FirewalledResponse validates and blocks any CRLF characters if added to response object.
* */

public class FirewallFilter implements Filter {

    private HttpFirewall firewall = new StrictHttpFirewall();

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

