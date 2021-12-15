package com.wavemaker.runtime.rest;

import java.util.function.Supplier;

public class RestExecutor {

    private static ThreadLocal<RequestContext> requestContextThreadLocal = ThreadLocal.withInitial(() -> RequestContext.Builder.newInstance().build());

    public static RequestContext getRequestContextThreadLocal() {
        return requestContextThreadLocal.get();
    }

    public <T> T executeWithContext(RequestContext requestContext, Supplier<T> supplier) {
        RequestContext oldRequestContext = getRequestContextThreadLocal();
        try {
            requestContextThreadLocal.set(requestContext);
            return supplier.get();
        } finally {
            requestContextThreadLocal.set(oldRequestContext);
        }
    }

    public void executeWithContext(RequestContext requestContext, Runnable runnable) {
        RequestContext oldRequestContext = getRequestContextThreadLocal();
        try {
            requestContextThreadLocal.set(requestContext);
            runnable.run();
        } finally {
            requestContextThreadLocal.set(oldRequestContext);
        }
    }

    public <T> T executeWithBasicAuth(String username, String password, Supplier<T> supplier) {
        return executeWithContext(RequestContext.Builder.newInstance().addBasicAuth(username, password).build(), supplier);
    }

    public void executeWithBasicAuth(String username, String password, Runnable runnable) {
        executeWithContext(RequestContext.Builder.newInstance().addBasicAuth(username, password).build(), runnable);
    }

    public <T> T executeWithJwt(String token, Supplier<T> supplier) {
        return executeWithContext(RequestContext.Builder.newInstance().addJwtToken(token).build(), supplier);
    }

    public void executeWithJwt(String token, Runnable runnable) {
        executeWithContext(RequestContext.Builder.newInstance().addJwtToken(token).build(), runnable);
    }
}
