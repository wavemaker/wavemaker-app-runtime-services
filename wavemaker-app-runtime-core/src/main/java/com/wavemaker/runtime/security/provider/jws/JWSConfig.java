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

package com.wavemaker.runtime.security.provider.jws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

import com.wavemaker.runtime.security.jws.JWSAuthenticationManagerResolver;

@Configuration
public class JWSConfig {
    @Bean(name = "bearerTokenAuthenticationFilter")
    public BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter() {
        return new BearerTokenAuthenticationFilter(jwtIssuerAuthenticationManagerResolver());
    }

    @Bean(name = "jwtIssuerAuthenticationManagerResolver")
    public JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver() {
        return new JwtIssuerAuthenticationManagerResolver(jwsAuthenticationManagerResolver());
    }

    @Bean(name = "jwsAuthenticationManagerResolver")
    public JWSAuthenticationManagerResolver jwsAuthenticationManagerResolver() {
        return new JWSAuthenticationManagerResolver();
    }
}
