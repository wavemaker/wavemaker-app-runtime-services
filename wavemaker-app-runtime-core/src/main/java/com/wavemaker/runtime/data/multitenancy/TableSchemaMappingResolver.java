package com.wavemaker.runtime.data.multitenancy;

import java.util.List;

import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateTemplate;

import com.wavemaker.commons.WMRuntimeException;

public class TableSchemaMappingResolver implements TenantSchemaMappingResolver {

    private static final String TABLE_MARKER = "table";
    private static final String TENANT_ID_COLUMN_MARKER = "_tenantIdColumn_";
    private static final String SCHEMA_NAME_COLUMN_MARKER = "_schemaNameColumn_";
    private static final String SCHEMA_NAME_BY_TENANT_ID_QUERY = "SELECT " + SCHEMA_NAME_COLUMN_MARKER + " FROM " + TABLE_MARKER + " WHERE " + TENANT_ID_COLUMN_MARKER + " = ?";
    private static final String Q_MARK = "\\?";
    private HibernateTemplate hibernateTemplate;
    private String tableName;
    private String schemaColumnName;
    private String tenantIdColumnName;

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setSchemaColumnName(String schemaColumnName) {
        this.schemaColumnName = schemaColumnName;
    }

    public void setTenantIdColumnName(String tenantIdColumnName) {
        this.tenantIdColumnName = tenantIdColumnName;
    }

    @Override
    public String getSchemaName(String tenantId) {
        String query = modifyQuery(tenantId);
        return executeQuery(query);
    }

    private String modifyQuery(String tenantId) {
        String query = SCHEMA_NAME_BY_TENANT_ID_QUERY;
        query = query.replaceAll(TABLE_MARKER, tableName);
        query = query.replaceAll(TENANT_ID_COLUMN_MARKER, tenantIdColumnName);
        query = query.replaceAll(SCHEMA_NAME_COLUMN_MARKER, schemaColumnName);
        query = query.replaceAll(Q_MARK, tenantId);
        return query;
    }

    private String executeQuery(String query) {
        Session session = hibernateTemplate.getSessionFactory().openSession();
        List list = session.createNativeQuery(query).list();
        session.close();
        if (list.isEmpty()) {
            throw new WMRuntimeException("No schema name mapped for the tenant id");
        }
        return list.get(0).toString();
    }

}
