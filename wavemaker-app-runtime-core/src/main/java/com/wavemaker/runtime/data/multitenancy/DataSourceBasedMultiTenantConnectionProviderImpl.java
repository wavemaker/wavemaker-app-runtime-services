package com.wavemaker.runtime.data.multitenancy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.service.spi.Stoppable;

public class DataSourceBasedMultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl implements Stoppable {

    private DataSource defaultDataSource;
    private Map<String, DataSource> dataSourcesMap = Collections.synchronizedMap(new HashMap());
    private DataSourceMapping dataSourceMapping;

    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public void setDataSourceMapping(DataSourceMapping dataSourceMapping) {
        this.dataSourceMapping = dataSourceMapping;
    }

    @PostConstruct
    public void load() {
        dataSourcesMap.put("DEFAULT_DATA_SOURCE", defaultDataSource);
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return dataSourcesMap.get("DEFAULT_DATA_SOURCE");
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        if (tenantIdentifier.equals(CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT)) {
            return selectAnyDataSource();
        } else {
            return getDataSource(tenantIdentifier);
        }
    }

    @Override
    public void stop() {

    }

    private DataSource getDataSource(String tenantIdentifier) {
        if (dataSourcesMap.containsKey(tenantIdentifier)) {
            return dataSourcesMap.get(tenantIdentifier);
        } else {
            DataSource dataSource = dataSourceMapping.getDataSource(tenantIdentifier);
            dataSourcesMap.put(tenantIdentifier, dataSource);
            return dataSource;
        }
    }
}
