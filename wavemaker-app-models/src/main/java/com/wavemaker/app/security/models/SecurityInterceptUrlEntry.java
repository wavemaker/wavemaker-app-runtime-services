/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models;

import com.wavemaker.app.web.http.HttpMethod;

public class SecurityInterceptUrlEntry {

    private String urlPattern;
    private Permission permission;
    private String[] roles;
    private HttpMethod httpMethod;

    public SecurityInterceptUrlEntry() {
    }

    public SecurityInterceptUrlEntry(String urlPattern, Permission permission) {
        this(urlPattern, permission, null);
    }

    public SecurityInterceptUrlEntry(String urlPattern, HttpMethod httpMethod, Permission permission) {
        this(urlPattern, httpMethod, permission, null);
    }

    public SecurityInterceptUrlEntry(String urlPattern, Permission permission, String[] roles) {
        this(urlPattern, null, permission, roles);
    }

    public SecurityInterceptUrlEntry(String urlPattern, HttpMethod httpMethod, Permission permission, String[] roles) {
        this.urlPattern = urlPattern;
        this.httpMethod = httpMethod;
        this.permission = permission;
        this.roles = roles;
    }

    public String getUrlPattern() {
        return this.urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public void setPermission(Permission attributes) {
        this.permission = attributes;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

}
