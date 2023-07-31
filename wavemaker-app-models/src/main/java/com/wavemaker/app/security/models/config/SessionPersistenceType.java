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
