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
package com.wavemaker.runtime.ssl;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Uday Shankar
 */
public class HostnameVerifierFactoryBean implements FactoryBean<HostnameVerifier> {
    
    @Override
    public HostnameVerifier getObject() throws Exception {
        return NoopHostnameVerifier.INSTANCE;
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
