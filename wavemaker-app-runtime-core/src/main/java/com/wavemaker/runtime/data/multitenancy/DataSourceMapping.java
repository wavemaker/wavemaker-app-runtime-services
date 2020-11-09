package com.wavemaker.runtime.data.multitenancy;

import javax.sql.DataSource;

public interface DataSourceMapping {
    DataSource getDataSource(String tenantId);
}
