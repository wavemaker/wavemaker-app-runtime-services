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

package com.wavemaker.runtime.data.task;

import org.hibernate.Session;

import com.wavemaker.common.util.ObjectUtils;
import com.wavemaker.runtime.data.DataServiceMetaData;
import com.wavemaker.runtime.data.Task;
import com.wavemaker.runtime.data.util.SystemUtils;

/**
 * @author Simon Toens
 */
public class MergeForUpdateTask extends BaseTask implements Task {

    @Override
    public Object run(Session session, String dbName, Object... input) {

        if (ObjectUtils.isNullOrEmpty(input)) {
            return input;
        }

        Object[] rtn = new Object[input.length];

        DataServiceMetaData metaData = getMetaData(dbName);

        for (int i = 0; i < input.length; i++) {

            rtn[i] = SystemUtils.serverMergeForUpdate(input[i], session, metaData);
        }

        return rtn;

    }

    @Override
    public String getName() {
        return "Built-in MergeForUpdate Task";
    }

}
