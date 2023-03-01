package com.wavemaker.runtime.session;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;

import javax.annotation.PostConstruct;

public class WMInMemorySessionRepository implements FindByIndexNameSessionRepository<MapSession> {

    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private Cache<String, Session> sessions;
    private Integer defaultMaxInactiveInterval;

    @PostConstruct
    public void init() {
        this.sessions = CacheBuilder.newBuilder().expireAfterAccess(defaultMaxInactiveInterval, TimeUnit.SECONDS).build();
    }

    @Override
    public Map<String, MapSession> findByIndexNameAndIndexValue(String indexName, String value) {
        if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return Collections.emptyMap();
        }
        Map<String, MapSession> sessionMap = new HashMap<>();
        sessions.asMap().values().stream().filter(session -> Objects.equals(getPrincipal(session), value))
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
            this.sessions.invalidate(session.getOriginalId());
        }
        this.sessions.put(session.getId(), new MapSession(session));
    }

    @Override
    public MapSession findById(String id) {
        Session saved = this.sessions.getIfPresent(id);
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
        this.sessions.invalidate(id);
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
