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
package com.wavemaker.runtime.ssl;

import javax.net.ssl.HostnameVerifier;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.runtime.rest.service.SSLContextProvider;

/**
 * @author Uday Shankar
 */
public class HostnameVerifierFactoryBean implements FactoryBean<HostnameVerifier> {

    @Autowired
    private SSLContextProvider sslContextProvider;

    @Override
    public HostnameVerifier getObject() throws Exception {
        return sslContextProvider.getHostnameVerifier();
    }

    @Override
    public Class<?> getObjectType() {
        return HostnameVerifier.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
