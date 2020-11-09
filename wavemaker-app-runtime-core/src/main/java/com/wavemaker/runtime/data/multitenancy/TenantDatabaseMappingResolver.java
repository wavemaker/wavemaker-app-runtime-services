package com.wavemaker.runtime.data.multitenancy;

public interface TenantDatabaseMappingResolver {
    String getDatabaseName(String tenantId);
}
