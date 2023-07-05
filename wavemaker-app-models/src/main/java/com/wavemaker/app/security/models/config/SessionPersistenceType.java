/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models.config;

public enum SessionPersistenceType {
    IN_MEMORY("in-memory", "inMemorySessionConfigHandler"),
    REDIS("redis", "redisSessionConfigHandler"),
    JDBC("jdbc", "jdbcSessionConfigHandler"),
    MONGODB("mongodb", "mongoDBSessionConfigHandler");

    private String name;
    private String handlerName;

    SessionPersistenceType(String name, String handlerName) {
        this.name = name;
        this.handlerName = handlerName;
    }

    public static SessionPersistenceType getSessionPersistenceType(String type) {
        for (SessionPersistenceType persistenceType : SessionPersistenceType.values()) {
            if (persistenceType.name.equalsIgnoreCase(type)) {
                return persistenceType;
            }
        }
        throw new IllegalArgumentException("No db type found for " + type);
    }

    public String getHandler() {
        return handlerName;
    }

    public String getName() {
        return name;
    }
}
