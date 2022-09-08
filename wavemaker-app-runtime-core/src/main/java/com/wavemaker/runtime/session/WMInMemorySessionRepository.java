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

package com.wavemaker.runtime.session;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;

public class WMInMemorySessionRepository implements FindByIndexNameSessionRepository<MapSession> {

    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private final Map<String, Session> sessions;
    private Integer defaultMaxInactiveInterval;

    public WMInMemorySessionRepository(Map<String, Session> sessions) {
        if (sessions == null) {
            throw new IllegalArgumentException("sessions cannot be null");
        } else {
            this.sessions = sessions;
        }
    }

    @Override
    public Map<String, MapSession> findByIndexNameAndIndexValue(String indexName, String value) {
        if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return Collections.emptyMap();
        }
        Map<String, MapSession> sessionMap = new HashMap<>();
        sessions.values().stream().filter(session -> Objects.equals(getPrincipal(session), value))
                .forEach(session -> sessionMap.put(session.getId(), (MapSession) session));
        return sessionMap;
    }

    @Override
    public MapSession createSession() {
        MapSession result = new MapSession();
        if (this.defaultMaxInactiveInterval != null) {
            result.setMaxInactiveInterval(Duration.ofSeconds((long) this.defaultMaxInactiveInterval));
        }
        return result;
    }

    @Override
    public void save(MapSession session) {
        if (!session.getId().equals(session.getOriginalId())) {
            this.sessions.remove(session.getOriginalId());
        }
        this.sessions.put(session.getId(), new MapSession(session));
    }

    @Override
    public MapSession findById(String id) {
        Session saved = this.sessions.get(id);
        if (saved == null) {
            return null;
        } else if (saved.isExpired()) {
            this.deleteById(saved.getId());
            return null;
        } else {
            return new MapSession(saved);
        }
    }

    @Override
    public void deleteById(String id) {
        this.sessions.remove(id);
    }

    public void setDefaultMaxInactiveInterval(int defaultMaxInactiveInterval) {
        this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
    }

    private Object getPrincipal(Session session) {
        Object principal = null;
        SecurityContext securityContext = session.getAttribute(SPRING_SECURITY_CONTEXT);
        if (securityContext != null) {
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null) {
                principal = authentication.getPrincipal();
            }
        }
        return principal;
    }
}
