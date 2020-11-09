package com.wavemaker.runtime.data.multitenancy;

import java.util.List;

import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateTemplate;

import com.wavemaker.commons.WMRuntimeException;

public class QuerySchemaMappingResolver implements TenantSchemaMappingResolver {

    private static final String LOGGED_IN_TENANT_ID = "LOGGED_IN_TENANT_ID";
    private HibernateTemplate hibernateTemplate;
    private boolean hql;
    private String query;

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public void setHql(boolean hql) {
        this.hql = hql;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String getSchemaName(String tenantId) {
        return executeQuery(tenantId);
    }

    private String executeQuery(String tenantId) {
        Session session = hibernateTemplate.getSessionFactory().openSession();
        List list = null;
        if (hql) {
            list = session.createQuery(query).setParameter(LOGGED_IN_TENANT_ID, tenantId).list();
        } else {
            list = session.createNativeQuery(query).setParameter(LOGGED_IN_TENANT_ID, tenantId).list();
        }
        session.close();
        if (list.isEmpty()) {
            throw new WMRuntimeException("No schema name mapped for the tenant id");
        }
        return list.get(0).toString();
    }
}
