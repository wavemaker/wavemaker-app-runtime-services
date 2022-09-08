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
package com.wavemaker.runtime.servicedef.model;

import java.util.Map;

import com.wavemaker.commons.auth.oauth2.OAuth2ProviderConfig;
import com.wavemaker.commons.servicedef.model.ServiceDefinition;
import com.wavemaker.runtime.security.xss.XssDisable;

@XssDisable
public class ServiceDefinitionsWrapper {

    private Map<String, ServiceDefinition> serviceDefs;

    private Map<String, Map<String, OAuth2ProviderConfig>> securityDefinitions;

    public Map<String, ServiceDefinition> getServiceDefs() {
        return serviceDefs;
    }

    public void setServiceDefs(Map<String, ServiceDefinition> serviceDefs) {
        this.serviceDefs = serviceDefs;
    }

    public Map<String, Map<String, OAuth2ProviderConfig>> getSecurityDefinitions() {
        return securityDefinitions;
    }

    public void setSecurityDefinitions(Map<String, Map<String, OAuth2ProviderConfig>> securityDefinitions) {
        this.securityDefinitions = securityDefinitions;
    }
}
