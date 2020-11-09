package com.wavemaker.runtime.data.multitenancy;

public interface TenantSchemaMappingResolver {
    String getSchemaName(String tenantId);
}
