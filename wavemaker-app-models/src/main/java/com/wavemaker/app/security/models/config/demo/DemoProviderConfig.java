/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config.demo;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.wavemaker.app.security.models.config.AbstractProviderConfig;
import com.wavemaker.app.security.models.DemoUser;

/**
 * @author Ed Callahan
 * @author Frankie Fu
 */
public class DemoProviderConfig extends AbstractProviderConfig {

    public static final String DEMO = "DEMO";

    @Valid
    @NotEmpty
    private List<DemoUser> users;

    @Override
    public String getType() {
        return DEMO;
    }

    public List<DemoUser> getUsers() {
        if (this.users == null) {
            return Collections.emptyList();
        }
        return this.users;
    }

    public void setUsers(List<DemoUser> users) {
        this.users = users;
    }
}
