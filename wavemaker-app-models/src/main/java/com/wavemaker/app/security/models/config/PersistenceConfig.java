/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wavemaker.app.security.models.config.session.JdbcPersistenceConfig;
import com.wavemaker.app.security.models.config.session.MongoPersistenceConfig;
import com.wavemaker.app.security.models.config.session.RedisPersistenceConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RedisPersistenceConfig.class, name = RedisPersistenceConfig.REDIS),
    @JsonSubTypes.Type(value = JdbcPersistenceConfig.class, name = JdbcPersistenceConfig.JDBC),
    @JsonSubTypes.Type(value = MongoPersistenceConfig.class, name = MongoPersistenceConfig.MONGODB)
})
public interface PersistenceConfig {

    String getType();

}
