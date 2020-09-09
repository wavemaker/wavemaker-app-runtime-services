/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.filter.etag;

import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Created by srujant on 26/3/19.
 */
public class CacheFilterConfig {

    private RequestMatcher cacheRequestMatcher;
    private RequestMatcher cacheExclusionRequestMatcher;
    private RequestMatcher etagRequestMatcher;

    public CacheFilterConfig() {
    }

    public RequestMatcher getCacheRequestMatcher() {
        return cacheRequestMatcher;
    }

    public void setCacheRequestMatcher(RequestMatcher cacheRequestMatcher) {
        this.cacheRequestMatcher = cacheRequestMatcher;
    }

    public RequestMatcher getEtagRequestMatcher() {
        return etagRequestMatcher;
    }

    public void setEtagRequestMatcher(RequestMatcher etagRequestMatcher) {
        this.etagRequestMatcher = etagRequestMatcher;
    }

    public RequestMatcher getCacheExclusionRequestMatcher() {
        return cacheExclusionRequestMatcher;
    }

    public void setCacheExclusionRequestMatcher(RequestMatcher cacheExclusionRequestMatcher) {
        this.cacheExclusionRequestMatcher = cacheExclusionRequestMatcher;
    }
}
