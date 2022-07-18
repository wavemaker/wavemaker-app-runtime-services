package com.wavemaker.runtime.web;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class SkipEtagHttpServletResponseWrapper extends HttpServletResponseWrapper {

    public SkipEtagHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setHeader(String name, String value) {
        if (!"etag".equalsIgnoreCase(name) && !"Last-Modified".equalsIgnoreCase(name)) {
            super.setHeader(name, value);
        }
    }
}
