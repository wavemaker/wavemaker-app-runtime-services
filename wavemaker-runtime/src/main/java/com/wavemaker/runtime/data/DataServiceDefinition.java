/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.NamedQueryDefinition;

import com.wavemaker.common.util.StringUtils;
import com.wavemaker.runtime.data.hibernate.DataServiceMetaData_Hib;
import com.wavemaker.runtime.data.util.DataServiceConstants;
import com.wavemaker.runtime.data.util.DataServiceUtils;
import com.wavemaker.runtime.service.ElementType;
import com.wavemaker.runtime.service.ServiceType;
import com.wavemaker.runtime.service.definition.AbstractDeprecatedServiceDefinition;
import com.wavemaker.runtime.service.definition.ReflectServiceDefinition;

/**
 * @author Simon Toens
 */
public class DataServiceDefinition extends AbstractDeprecatedServiceDefinition implements DataServiceInternal, ReflectServiceDefinition {

    private ElementTypeFactory elementTypeFactory = DEFAULT_ELEMENT_TYPE_FACTORY;

    private final DataServiceMetaData metaData;

    private SessionFactory sessionFactory = null;

    // get query meta data that Hibernate API doesn't have
    // access to
    private ExternalDataModelConfig externalConfig = null;

    private static String getName(String cfgfile) {
        cfgfile = cfgfile.replace("\\", "/");
        cfgfile = StringUtils.fromLastOccurrence(cfgfile, "/");
        return StringUtils.fromFirstOccurrence(cfgfile, ".", -1);
    }

    /**
     * Load configuration from classpath, using given resource name.
     */
    public DataServiceDefinition(String hbConfFile, Properties p, boolean useIndividualCRUDOperations) {
        this(getName(hbConfFile), DataServiceUtils.initConfiguration(hbConfFile, p), useIndividualCRUDOperations);
    }

    public DataServiceDefinition(DataServiceMetaData metaData) {
        this.metaData = metaData;
    }

    public DataServiceDefinition(String serviceName, Configuration hbcfg, boolean isImportDB, boolean useIndividualCRUDOperations) {
        this.metaData = new DataServiceMetaData_Hib(serviceName, hbcfg);

        try {
            this.sessionFactory = hbcfg.buildSessionFactory();
            Session session = null;
            try {
                session = this.sessionFactory.openSession();
                this.metaData.init(session, useIndividualCRUDOperations);
            } finally {
                try {
                    session.close();
                } catch (RuntimeException ignore) {
                }
            }
        } catch (RuntimeException ex) {
            if (isImportDB) {
                // the following happens during import db - so far looks like
                // it can be ignored:
                // java.lang.NullPointerException
                // at org.hibernate.mapping.PersistentClass.
                // prepareTemporaryTables(PersistentClass.java:737)
            } else {
                throw ex;
            }
        }
    }

    private DataServiceDefinition(String serviceName, Configuration hbcfg, boolean useIndividualCRUDOperations) {
        this(serviceName, hbcfg, false, useIndividualCRUDOperations);
    }

    @Override
    public void setExternalConfig(ExternalDataModelConfig externalConfig) {
        this.externalConfig = externalConfig;
    }

    public DataServiceMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public void setElementTypeFactory(ElementTypeFactory elementTypeFactory) {
        this.elementTypeFactory = elementTypeFactory;
    }

    @Override
    public List<ElementType> getInputTypes(String operationName) {

        DataServiceOperation op = this.metaData.getOperation(operationName);

        List<String> inputNames = op.getInputNames();
        List<String> inputTypes = op.getInputTypes();
        List<Boolean> inputIsList = op.getInputIsList();

        List<ElementType> rtn = new ArrayList<ElementType>(inputTypes.size());

        for (int i = 0; i < inputTypes.size(); i++) {
            ElementType et = DEFAULT_ELEMENT_TYPE_FACTORY.getElementType(inputTypes.get(i));
            et.setName(inputNames.get(i));
            et.setList(inputIsList.get(i));
            rtn.add(et);
        }

        return rtn;
    }

    @Override
    public List<String> getOperationNames() {
        return new ArrayList<String>(this.metaData.getOperationNames());
    }

    @Override
    public ElementType getOutputType(String operationName) {

        DataServiceOperation op = this.metaData.getOperation(operationName);

        String outputType = op.getOutputType();

        if (outputType == null) {
            return null;
        }

        ElementType rtn = DEFAULT_ELEMENT_TYPE_FACTORY.getElementType(outputType);
        rtn.setName("rtn");

        // this is quite confusing
        // get returnsSingleResult from DataModelConfig
        if (op.isQuery() && this.externalConfig != null) {
            rtn.setList(!this.externalConfig.returnsSingleResult(operationName));
        } else {
            rtn.setList(!op.getReturnsSingleResult());
        }
        return rtn;
    }

    @Override
    public String getOperationType(String operationName) {
        String type;

        NamedQueryDefinition def = this.metaData.getHqlQueryDefinition(operationName);

        if (def == null) {
            def = this.metaData.getSqlQueryDefinition(operationName);
            if (def == null) {
                type = "other";
            } else {
                type = "sqlquery";
            }
        } else {
            type = "hqlquery";
        }
        return type;
    }

    @Override
    public String getPackageName() {
        if (this.metaData.getServiceClassName() != null) {
            return StringUtils.splitPackageAndClass(this.metaData.getServiceClassName()).v1;
        } else {
            throw new AssertionError("Metadata service class must be set");
        }
    }

    @Override
    public String getDataPackage() {
        return this.metaData.getDataPackage();
    }

    @Override
    public String getServiceId() {
        return this.metaData.getName();
    }

    @Override
    public ServiceType getServiceType() {
        return new DataServiceType();
    }

    @Override
    public String getRuntimeConfiguration() {
        return getServiceId() + DataServiceConstants.SPRING_CFG_EXT;
    }

    @Override
    public void dispose() {
        this.metaData.dispose();
        try {
            if (this.sessionFactory != null) {
                this.sessionFactory.close();
            }
        } catch (RuntimeException ignore) {
        }
    }

    @Override
    public String getServiceClass() {
        // at import-db time, the meta-data has the service
        // class name to use. this is to allow a class
        // name that is different from the serviceid.
        // we may not need this, since the tooling is
        // built such that the class name is always the
        // same as the serviceid.
        if (this.metaData.getServiceClassName() != null) {
            return this.metaData.getServiceClassName();
        } else {
            throw new AssertionError("Metadata service class must be set");
        }
    }

    @Override
    public List<ElementType> getTypes() {
        Collection<String> entities = this.metaData.getEntityClassNames();
        Collection<String> helperTypes = this.metaData.getHelperClassNames();
        return DataServiceUtils.getTypes(entities, helperTypes, this.elementTypeFactory);
    }

    @Override
    public List<ElementType> getTypes(String username, String password) {
        Collection<String> entities = this.metaData.getEntityClassNames();
        Collection<String> helperTypes = this.metaData.getHelperClassNames();
        return DataServiceUtils.getTypes(entities, helperTypes, this.elementTypeFactory);
    }

    public String outputTypeToString(String operationName) {
        ElementType et = getOutputType(operationName);
        return et.getJavaType();
    }

    public String inputTypesToString(String operationName) {
        StringBuilder rtn = new StringBuilder();
        for (ElementType et : getInputTypes(operationName)) {
            rtn.append(et.getJavaType()).append(" ");
            rtn.append("list: " + et.isList());
        }
        return rtn.toString();
    }

    @Override
    public List<String> getEventNotifiers() {
        return Collections.emptyList();
    }

    @Override
    public DataServiceOperation getOperation(String operationName) {
        return this.metaData.getOperation(operationName);
    }

    @Override
    public boolean isLiveDataService() {
        return true;
    }

    @Override
    public String getPartnerName() {
        return null;
    }
}
