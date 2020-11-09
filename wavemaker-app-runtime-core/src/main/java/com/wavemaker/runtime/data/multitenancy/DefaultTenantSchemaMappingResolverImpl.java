package com.wavemaker.runtime.data.multitenancy;

public class DefaultTenantSchemaMappingResolverImpl implements TenantSchemaMappingResolver {

    @Override
    public String getSchemaName(String tenantId) {
        return tenantId;
    }

}
