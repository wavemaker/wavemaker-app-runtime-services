/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wavemaker.runtime.data.util;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.wavemaker.runtime.commons.WMAppContext;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 3/5/18
 */
public interface TransactionUtils {

    static <T> T executeInReadOnlyTransaction(String txManagerId, TransactionCallback<T> callback) {
        return executeInTransaction(txManagerId, true, callback);
    }

    static <T> T executeInTransaction(String txManagerId, boolean readOnly, TransactionCallback<T> callback) {
        PlatformTransactionManager transactionManager = WMAppContext.getInstance()
                .getSpringBean(txManagerId);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.setReadOnly(readOnly);

        return txTemplate.execute(callback);
    }
}
