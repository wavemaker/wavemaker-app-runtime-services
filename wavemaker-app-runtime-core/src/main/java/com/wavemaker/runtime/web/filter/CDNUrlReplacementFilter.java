package com.wavemaker.runtime.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.runtime.web.wrapper.CDNUrlReplacementServletResponseWrapper;

public class CDNUrlReplacementFilter extends GenericFilterBean {


    private static final String CDN_URL_PLACEHOLDER = "_cdnUrl_";

    private static final Logger cdnUrlReplacementFilterLogger = LoggerFactory.getLogger(CDNUrlReplacementFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        if (requestMatches(httpServletRequest)) {
            String cdnUrl = servletRequest.getServletContext().getInitParameter("cdnUrl");
            cdnUrlReplacementFilterLogger.debug("Replacing _cdnUrl_ placeholder with the value : {}", cdnUrl);
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            CDNUrlReplacementServletResponseWrapper cdnUrlReplacementServletResponseWrapper = new CDNUrlReplacementServletResponseWrapper(httpServletResponse);
            chain.doFilter(httpServletRequest, cdnUrlReplacementServletResponseWrapper);
            String response = new String(cdnUrlReplacementServletResponseWrapper.getByteArray());
            response = response.replace(CDN_URL_PLACEHOLDER, cdnUrl);
            httpServletResponse.setContentLengthLong(response.getBytes().length);
            httpServletResponse.getWriter().write(response);
            return;
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    protected boolean requestMatches(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getServletPath().equals("/") || httpServletRequest.getServletPath().equals("/index.html");
    }
}