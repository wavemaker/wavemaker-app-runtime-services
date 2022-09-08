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
package com.wavemaker.runtime.security.provider.ldap;

import java.util.Arrays;
import java.util.Hashtable;

import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

/**
 * @author Kishore Routhu on 5/1/18 2:29 PM.
 */
public class WMSpringSecurityContextSource extends DefaultSpringSecurityContextSource {

    private static final String CONNECT_TIMEOUT_ENV_PROPERTY = "com.sun.jndi.ldap.connect.timeout";
    private static final String READ_TIMEOUT_ENV_PROPERTY = "com.sun.jndi.ldap.read.timeout";
    private static final String URL_SEPARATOR = " ";

    private static final String DEFAULT_TIME_OUT = "30000";

    public WMSpringSecurityContextSource(String providerUrl) {
        super(providerUrl);
    }

    public WMSpringSecurityContextSource(String providerUrl, String baseDn) {
        super(Arrays.asList(providerUrl.split(URL_SEPARATOR)), baseDn);
    }

    @Override
    protected Hashtable<String, Object> getAuthenticatedEnv(String principal, String credentials) {
        Hashtable<String, Object> authenticatedEnv = super.getAuthenticatedEnv(principal, credentials);
        authenticatedEnv.put(CONNECT_TIMEOUT_ENV_PROPERTY, DEFAULT_TIME_OUT);
        authenticatedEnv.put(READ_TIMEOUT_ENV_PROPERTY, DEFAULT_TIME_OUT);
        return authenticatedEnv;
    }
}
