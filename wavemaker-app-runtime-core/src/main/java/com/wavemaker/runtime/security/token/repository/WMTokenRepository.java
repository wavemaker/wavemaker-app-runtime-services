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
package com.wavemaker.runtime.security.token.repository;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wavemaker.commons.model.security.TokenAuthConfig;
import com.wavemaker.runtime.security.WMUser;

/**
 * Created by prakashb on 2/1/19.
 */
public class WMTokenRepository implements TokenRepository {

    @Autowired
    private TokenAuthConfig tokenAuthConfig;

    public static final int DEFAULT_VALIDITY_SECONDS = 1800;

    private static final TimeUnit SECONDS = TimeUnit.SECONDS;

    private int tokenValiditySeconds = DEFAULT_VALIDITY_SECONDS;

    private Cache<String, WMUser> tokenVsWMUser;

    @PostConstruct
    public void init() {
        tokenValiditySeconds = tokenAuthConfig.getTokenValiditySeconds();
        tokenVsWMUser = CacheBuilder.newBuilder().expireAfterWrite(tokenValiditySeconds, SECONDS).build();
    }

    @Override
    public void addToken(String token, WMUser wmUser) {
        tokenVsWMUser.put(token, wmUser);
    }

    @Override
    public WMUser loadUser(String token) {
        return tokenVsWMUser.getIfPresent(token);
    }

    @Override
    public void removeUser(String token) {
        tokenVsWMUser.invalidate(token);
    }
}
