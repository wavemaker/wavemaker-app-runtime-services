package com.wavemaker.runtime.servicedef.model;

import java.util.Map;

import com.wavemaker.commons.auth.oauth2.OAuth2ProviderConfig;
import com.wavemaker.commons.servicedef.model.ServiceDefinition;

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
