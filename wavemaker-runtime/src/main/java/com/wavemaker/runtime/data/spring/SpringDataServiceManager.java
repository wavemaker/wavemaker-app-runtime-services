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

package com.wavemaker.runtime.data.spring;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.wavemaker.common.WMRuntimeException;
import org.apache.commons.logging.Log;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.wavemaker.common.util.StringUtils;
import com.wavemaker.runtime.WMAppContext;
import com.wavemaker.runtime.data.DataServiceLoggers;
import com.wavemaker.runtime.data.DataServiceManager;
import com.wavemaker.runtime.data.DataServiceMetaData;
import com.wavemaker.runtime.data.DataServiceRuntimeException;
import com.wavemaker.runtime.data.DefaultTaskManager;
import com.wavemaker.runtime.data.Task;
import com.wavemaker.runtime.data.TaskManager;
import com.wavemaker.runtime.data.ThreadContext;
import com.wavemaker.runtime.data.hibernate.DataServiceMetaData_Hib;
import com.wavemaker.runtime.data.task.DefaultRollback;
import com.wavemaker.runtime.data.task.PreProcessor;
import com.wavemaker.runtime.data.util.DataServiceConstants;
import com.wavemaker.runtime.data.util.QueryHandler;

/**
 * @author Simon Toens
 */
public class SpringDataServiceManager implements DataServiceManager {

    public static final Log txLogger = DataServiceLoggers.transactionLogger;

    private final PlatformTransactionManager txMgr;

    private final HibernateTemplate hibernateTemplate;

    private final TaskManager taskMgr;

    private final DataServiceMetaData metaData;

    private final Configuration cfg;

    private final SessionFactory sessionFactory;

    public SpringDataServiceManager(String configurationName, HibernateTemplate hibernateTemplate, PlatformTransactionManager txMgr) {

        this(configurationName, hibernateTemplate, txMgr, DefaultTaskManager.getInstance(), Collections.<String, String> emptyMap());
    }

    public SpringDataServiceManager(String configurationName, HibernateTemplate hibernateTemplate, PlatformTransactionManager txMgr,
        TaskManager taskMgr, Map<String, String> properties) {
        if (configurationName == null) {
            throw new IllegalArgumentException("configurationName must be set");
        }

        if (hibernateTemplate == null) {
            throw new IllegalArgumentException("hibernateTemplate must be set");
        }

        if (txMgr == null) {
            throw new IllegalArgumentException("platformTransactionManager must be set");
        }

        if (taskMgr == null) {
            throw new IllegalArgumentException("taskManager must be set");
        }

        this.hibernateTemplate = hibernateTemplate;
        this.txMgr = txMgr;
        this.taskMgr = taskMgr;

        ConfigurationRegistry reg = ConfigurationRegistry.getInstance();
        this.cfg = reg.getConfiguration(configurationName);
        if (this.cfg == null) {
            throw new DataServiceRuntimeException("Cannot find configuration for " + configurationName);
        }

        this.sessionFactory = reg.getSessionFactory(configurationName);
        if (this.sessionFactory == null) {
            throw new DataServiceRuntimeException("Cannot find session factory for " + configurationName);
        }

        String s = properties.get(DataServiceConstants.GENERATE_OLD_STYLE_OPRS_PROPERTY);

        boolean useIndividualCRUDOperations = Boolean.valueOf(s);

        this.metaData = initMetaData(configurationName, this.cfg, hibernateTemplate, useIndividualCRUDOperations, properties);
    }

    @Override
    public DataServiceMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public Session getSession() {
        if (!isTxRunning()) {
            txLogger.warn("begin a tx before accessing the session");
        }
        return (Session) invoke(this.taskMgr.getSessionTask());
    }

    @Override
    public void begin() {
        if (txLogger.isInfoEnabled()) {
            txLogger.info("begin");
        }

        ThreadContext.Context ctx = ThreadContext.getContext(this.metaData.getName());
        if (ctx == null) {
            ctx = new ThreadContext.Context(this.txMgr, this.metaData, this.cfg, this.sessionFactory);
        }

        TransactionStatus txStatus = ctx.getTransactionStatus();

        if (txStatus == null) {
            txStatus = this.txMgr.getTransaction(null);
            ctx.setTransactionStatus(txStatus);
        } else {
            if (txLogger.isWarnEnabled()) {
                txLogger.warn("ignoring begin - tx already in progress");
                if (txLogger.isDebugEnabled()) {
                    logStackTrace();
                }
            }
        }
    }

    @Override
    public void commit() {

        if (txLogger.isInfoEnabled()) {
            txLogger.info("commit");
        }

        ThreadContext.Context ctx = ThreadContext.getContext(this.metaData.getName());

        if (ctx == null) {
            if (txLogger.isWarnEnabled()) {
                txLogger.warn("ignoring commit - no tx in progress");
                if (txLogger.isDebugEnabled()) {
                    logStackTrace();
                }
            }
            return;
        }

        TransactionStatus txStatus = ctx.getTransactionStatus();

        try {
            if (txStatus == null) {
                if (txLogger.isWarnEnabled()) {
                    txLogger.warn("ignoring commit - no tx status");
                    if (txLogger.isDebugEnabled()) {
                        logStackTrace();
                    }
                }
            } else {
                this.txMgr.commit(txStatus);
            }
        } finally {
            ctx.setTransactionStatus(null);
            ThreadContext.unsetContext(this.metaData.getName());
            HashMap<String, ThreadContext.Context> contextHash = ThreadContext.getThreadLocalHash();

            if (contextHash != null && contextHash.size() > 0) {
                if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.initSynchronization();
                }
            } else {
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.clear();
                    Map map = TransactionSynchronizationManager.getResourceMap();
                    for (Object entry : map.keySet()) {
                        TransactionSynchronizationManager.unbindResource(entry);
                    }
                }
            }
        }
    }

    @Override
    public void rollback() {

        if (txLogger.isInfoEnabled()) {
            txLogger.info("rollback");
        }

        ThreadContext.Context ctx = ThreadContext.getContext(this.metaData.getName());

        if (ctx == null) {
            if (txLogger.isWarnEnabled()) {
                txLogger.warn("ignoring rollback - no tx in progress");
                if (txLogger.isDebugEnabled()) {
                    logStackTrace();
                }
            }
            return;
        }

        TransactionStatus txStatus = ctx.getTransactionStatus();

        try {

            if (txStatus == null) {
                if (txLogger.isWarnEnabled()) {
                    txLogger.warn("ignoring rollback - no tx status");
                    if (txLogger.isDebugEnabled()) {
                        logStackTrace();
                    }
                }
            } else {
                this.txMgr.rollback(txStatus);
            }
        } finally {
            ctx.setTransactionStatus(null);
            ThreadContext.unsetContext(this.metaData.getName());
        }
    }

    @Override
    public Object invoke(Task task, Object... input) {
        boolean unset = false;
        ThreadContext.Context ctx = ThreadContext.getContext(this.metaData.getName());
        if (ctx == null) {
            ctx = new ThreadContext.Context(this.txMgr, this.metaData, this.cfg, this.sessionFactory);
            unset = true;
        }
        try {
            return runInTx(task, input);
        } finally {
            if (unset) {
                ThreadContext.unsetContext(this.metaData.getName());
            }
        }
    }

    @Override
    public void dispose() {
        try {
            if (isTxRunning()) {
                rollback();
            }
            // access sessionFactory here and close it too
            getMetaData().dispose();
        } catch (RuntimeException ignore) {
        }
    }

    private boolean isTxRunning() {
        ThreadContext.Context ctx = ThreadContext.getContext(this.metaData.getName());
        if (ctx == null) {
            return false;
        }
        return ctx.getTransactionStatus() != null;
    }

    private Object runInTx(Task task, Object... input) {
        HibernateCallback action = new RunInHibernate(task, input);
        TransactionTemplate txTemplate = new TransactionTemplate(this.txMgr);
        boolean rollbackOnly = task instanceof DefaultRollback && !isTxRunning();
        RunInTx tx = new RunInTx(action, rollbackOnly);
        if (txLogger.isInfoEnabled()) {
            if (isTxRunning()) {
                txLogger.info("tx is running executing \"" + task.getName() + "\" in current tx");
            } else {
                txLogger.info("no tx running, wrapping execution of \"" + task.getName() + "\" in tx");
                if (rollbackOnly) {
                    txLogger.info("rollback enabled for \"" + task.getName() + "\"");
                }
            }
        }
        Object rtn = null;
        try {
            rtn = txTemplate.execute(tx);
        } catch (Throwable ex) {
            //The following logic intends to display a sensible message for the user when a column contains a value whose length
            //exceeds the maximum length allowed in the database.  The logic has been tested on MySQL, Postgres, Oracle and
            //SQLServer so far.
            if (ex.getCause() instanceof java.sql.BatchUpdateException) { //Oracle
                String msg = ((java.sql.BatchUpdateException)ex.getCause()).getNextException().getMessage();
                if (msg != null) {
                    ex.printStackTrace();
                    throw new WMRuntimeException(msg);
                }
            } else if (ex.getCause().getCause() instanceof java.sql.BatchUpdateException) { //Postgres
                java.sql.BatchUpdateException e = (java.sql.BatchUpdateException)ex.getCause().getCause();
                if (e != null && e.getMessage() != null) {
                    ex.printStackTrace();
                    throw new WMRuntimeException(e.getNextException().getMessage());
                }
            } else if (ex.getCause().getCause() != null) { //MySQL, SQLServer
                String msg = ex.getCause().getCause().getMessage();
                if (msg != null) {
                    ex.printStackTrace();
                    throw new WMRuntimeException(msg);
                }
            } else {
                throw new WMRuntimeException(ex);
            }
        }
        if (txLogger.isInfoEnabled()) {
            if (isTxRunning()) {
                txLogger.info("tx is running after execution of \"" + task.getName() + "\"");
            } else {
                txLogger.info("tx is not running after execution of \"" + task.getName() + "\"");
            }

        }
        return rtn;
    }

    private void logStackTrace() {
        txLogger.debug(StringUtils.toString(new Throwable()));
    }

    private class RunInHibernate implements HibernateCallback {

        private final Task task;

        private Object[] input;

        RunInHibernate(Task task, Object[] input) {
            this.task = task;
            this.input = input;
        }

        @Override
        public Object doInHibernate(Session session) {

            Task preProcessorTask = ThreadContext.getPreProcessorTask();

            if (preProcessorTask != null && this.task instanceof PreProcessor) {
                if (txLogger.isInfoEnabled()) {
                    txLogger.info("Running preprocessor task " + preProcessorTask.getName());
                }
                this.input = (Object[]) preProcessorTask.run(session, SpringDataServiceManager.this.metaData.getName(), this.input, this.task,
                    SpringDataServiceManager.this.taskMgr);
            }

            WMAppContext wmApp = WMAppContext.getInstance();
            if (wmApp != null && wmApp.isMultiTenant()) {
                Class[] clsArr = new Class[] { Session.class };
                ClassLoader cl = Session.class.getClassLoader();
                QueryHandler qh = new QueryHandler(session, SpringDataServiceManager.this.metaData.getConfiguration());
                session = (Session) Proxy.newProxyInstance(cl, clsArr, qh);
            }

            return this.task.run(session, SpringDataServiceManager.this.metaData.getName(), this.input);
        }

    }

    private class RunInTx implements TransactionCallback {

        private final HibernateCallback action;

        private final boolean rollbackOnly;

        RunInTx(HibernateCallback action, boolean rollbackOnly) {
            this.action = action;
            this.rollbackOnly = rollbackOnly;
        }

        @Override
        public Object doInTransaction(TransactionStatus status) {
            if (this.rollbackOnly) {
                status.setRollbackOnly();
            }
            return SpringDataServiceManager.this.hibernateTemplate.execute(this.action);
        }
    }

    private static DataServiceMetaData initMetaData(String configurationName, Configuration cfg, HibernateTemplate htemp,
        final boolean useIndividualCRUDOperations, final Map<String, String> properties) {
        final DataServiceMetaData rtn = new DataServiceMetaData_Hib(configurationName, cfg, properties); // salesforce

        htemp.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                rtn.init(session, useIndividualCRUDOperations);
                return null;
            }
        });

        return rtn;
    }

    @Override
    public Object invoke(Task task, Map<String, Class<?>> types, boolean named, Object... input) { // salesforce
        return null;
    }

}
