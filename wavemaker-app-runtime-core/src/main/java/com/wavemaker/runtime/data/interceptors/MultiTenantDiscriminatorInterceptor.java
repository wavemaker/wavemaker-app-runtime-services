package com.wavemaker.runtime.data.interceptors;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.SecurityService;

public class MultiTenantDiscriminatorInterceptor extends EmptyInterceptor {

    private String tenantIdFieldName;
    private HibernateTemplate template;

    @Autowired
    private SecurityService securityService;

    public void setTenantIdFieldName(String tenantIdFieldName) {
        this.tenantIdFieldName = tenantIdFieldName;
    }

    public void setTemplate(HibernateTemplate template) {
        this.template = template;
    }

    @Override
    public String onPrepareStatement(String sql) {
        if (securityService.isAuthenticated()) {
            Object tenantIdObject = securityService.getTenantIdOfUser();
            if (tenantIdObject != null) {
                int tenantId = (int) tenantIdObject;
                Objects.requireNonNull(template.getSessionFactory()).getCurrentSession().
                        enableFilter("tenantIdFilter").
                        setParameter("tenantId", tenantId);
            }
        }
        return super.onPrepareStatement(sql);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        canCurrentUserAccess(previousState, propertyNames);
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        int indexOfTenantIdColumn = getIndexOfTenantIdColumn(propertyNames);
        if (indexOfTenantIdColumn != -1) {
            state[indexOfTenantIdColumn] = securityService.getTenantIdOfUser();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        canCurrentUserAccess(state, propertyNames);
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (securityService.isAuthenticated()) {
            if (securityService.getTenantIdOfUser() != null) {
                canCurrentUserAccess(state, propertyNames);
            }
        }
        return false;
    }

    private void canCurrentUserAccess(Object[] state, String[] propertyNames) {
        int indexOfTenantIdColumn = getIndexOfTenantIdColumn(propertyNames);
        if (indexOfTenantIdColumn != -1) {
            String tenantId = String.valueOf(state[indexOfTenantIdColumn]);
            if (!Objects.equals(tenantId, String.valueOf(securityService.getTenantIdOfUser()))) {
                throw new WMRuntimeException("User not authorized");
            }
        }
    }

    private int getIndexOfTenantIdColumn(String[] propertyNames) {
        for (int index = 0; index < propertyNames.length; index++) {
            if (propertyNames[index].equals(tenantIdFieldName)) {
                return index;
            }
        }
        return -1;
    }
}
