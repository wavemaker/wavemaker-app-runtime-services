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
package com.wavemaker.runtime.system;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;

import com.wavemaker.runtime.security.SecurityService;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 10/7/17
 */
@Component
public class ServerVariableValueProvider implements VariableValueProvider {

    private final SecurityService securityService;

    private Map<String, VariableValue> keyVsValueMap;

    @Autowired
    public ServerVariableValueProvider(final SecurityService securityService) {
        this.securityService = securityService;
        keyVsValueMap = new LinkedCaseInsensitiveMap<>();

        keyVsValueMap.put("date", key -> new Date(Calendar.getInstance().getTime().getTime()));
        keyVsValueMap.put("time", key -> new Time(Calendar.getInstance().getTime().getTime()));
        keyVsValueMap.put("date_time", key -> LocalDateTime.now()); // use java LocalDateTime
        keyVsValueMap.put("timestamp", key -> new Timestamp(Calendar.getInstance().getTime().getTime()));

        keyVsValueMap.put("user_id", key -> securityService.getUserId());
        keyVsValueMap.put("user_name", key -> securityService.getUserName());
    }

    @Override
    public Object getValue(final String variableName) {
        // XXX support for custom properties
        if (keyVsValueMap.containsKey(variableName)) {
            return keyVsValueMap.get(variableName).valueFor(variableName);
        }

        throw new IllegalArgumentException("No system variable matching with given name: " + variableName);
    }

}
