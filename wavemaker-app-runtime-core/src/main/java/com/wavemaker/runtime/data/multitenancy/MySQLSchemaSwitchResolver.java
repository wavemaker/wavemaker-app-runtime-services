package com.wavemaker.runtime.data.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQLSchemaSwitchResolver implements SchemaSwitchResolverByDBType {

    private static final Logger logger = LoggerFactory.getLogger(MySQLSchemaSwitchResolver.class);

    @Override
    public Connection switchSchema(Connection connection, String schemaName) {
        try {
            connection.createStatement().execute("USE " + schemaName);
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (Exception e1) {
                logger.warn("Failed to close connection", e1);
            }
            throw new HibernateException("Could not alter JDBC connection to specified schema [" + schemaName + "]", e);
        }
    }
}
