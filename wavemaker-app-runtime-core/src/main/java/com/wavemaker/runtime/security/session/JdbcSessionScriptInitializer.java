package com.wavemaker.runtime.security.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.data.datasource.WMDataSource;

public class JdbcSessionScriptInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSessionScriptInitializer.class);

    @Resource(name = "${session.jdbc.serviceName}DataSource")
    private WMDataSource dataSource;

    @PostConstruct
    public void initializeScript() {
        try {
            ResultSet resultSet;
            try (Connection connection = dataSource.getTargetDataSource().getConnection()) {
                resultSet = connection.getMetaData().getTables(null, null, "SPRING_SESSION", null);
            }
            if (resultSet.next()) {
                LOGGER.debug("Skipping SPRING_SESSION table creation as it already exists");
            } else {
                ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
                ClassPathResource classPathResource =
                        new ClassPathResource("org/springframework/session/jdbc/schema-" + extractDbType(dataSource.getConnection().getMetaData().getURL()) + ".sql");
                LOGGER.debug("executing sql script from resource: {}", classPathResource.getPath());
                resourceDatabasePopulator.addScript(classPathResource);
                resourceDatabasePopulator.execute(dataSource);
            }
        } catch (SQLException throwables) {
            throw new WMRuntimeException(throwables.getCause());
        }
    }

    private String extractDbType(String url) {
        String[] connectionUrl = url.split(":");
        return connectionUrl[1];
    }
}
