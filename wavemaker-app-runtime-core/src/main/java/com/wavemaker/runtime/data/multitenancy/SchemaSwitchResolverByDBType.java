package com.wavemaker.runtime.data.multitenancy;

import java.sql.Connection;

public interface SchemaSwitchResolverByDBType {
    Connection switchSchema(Connection connection,String schemaName);
}
