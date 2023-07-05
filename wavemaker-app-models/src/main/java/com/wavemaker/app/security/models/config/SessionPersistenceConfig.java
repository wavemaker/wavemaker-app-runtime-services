/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config;

import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wavemaker.app.security.models.annotation.ProfilizableProperty;

public class SessionPersistenceConfig {

    @NotNull
    @ProfilizableProperty("${security.session.persistence.type}")
    private SessionPersistenceType enabledType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NotEmpty
    //@Valid TODO UI is sending the map for all types irrespective of persistence type selected
    private Map<SessionPersistenceType, PersistenceConfig> config;

    public Map<SessionPersistenceType, PersistenceConfig> getConfig() {
        return config;
    }

    public void setConfig(Map<SessionPersistenceType, PersistenceConfig> config) {
        this.config = config;
    }

    public SessionPersistenceType getEnabledType() {
        return enabledType;
    }

    public void setEnabledType(SessionPersistenceType enabledType) {
        this.enabledType = enabledType;
    }
}
