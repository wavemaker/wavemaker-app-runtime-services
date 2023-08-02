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

package com.wavemaker.runtime.security.session;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.session.web.http.DefaultCookieSerializer;

public class DefaultCookieSerializerFactoryBean implements FactoryBean<DefaultCookieSerializer> {

    @Value("${security.general.cookie.path:#{null}}")
    private String cookiePath;

    @Value("${security.general.cookie.jvmRoute:#{null}}")
    private String jvmRoute;

    @Value("#{${security.general.cookie.maxAge:-1} * 60}")
    private int cookieMaxAge;

    @Value("${security.general.cookie.base64Encode:true}")
    private boolean base64Encode;

    @Value("${security.general.cookie.sameSite:#{null}}")
    private String sameSite;

    @Override
    public DefaultCookieSerializer getObject() throws Exception {
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        if (StringUtils.isNotBlank(cookiePath)) {
            defaultCookieSerializer.setCookiePath(cookiePath);
        }
        if (StringUtils.isNotBlank(jvmRoute)) {
            defaultCookieSerializer.setJvmRoute(jvmRoute);
        }
        if (StringUtils.isNotBlank(sameSite)) {
            defaultCookieSerializer.setSameSite(sameSite);
        } else {
            defaultCookieSerializer.setSameSite(null);
        }
        defaultCookieSerializer.setCookieMaxAge(cookieMaxAge);
        defaultCookieSerializer.setUseBase64Encoding(base64Encode);
        return defaultCookieSerializer;
    }

    @Override
    public Class<?> getObjectType() {
        return DefaultCookieSerializer.class;
    }
}
