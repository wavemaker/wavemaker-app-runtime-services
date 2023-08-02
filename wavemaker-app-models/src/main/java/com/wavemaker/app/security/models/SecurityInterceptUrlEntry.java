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
