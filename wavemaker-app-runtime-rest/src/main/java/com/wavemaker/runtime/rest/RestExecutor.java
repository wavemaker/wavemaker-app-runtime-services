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
