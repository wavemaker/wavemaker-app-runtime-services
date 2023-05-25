/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.security.models;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotEmpty;

public class DemoUser {
    private String userid;
    private String password;

    @NotEmpty
    private List<String> roles;

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        if (this.roles == null) {
            return Collections.emptyList();
        }
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "DemoUser{" +
            "userid='" + userid + '\'' +
            ", roles=" + roles +
            '}';
    }
}
