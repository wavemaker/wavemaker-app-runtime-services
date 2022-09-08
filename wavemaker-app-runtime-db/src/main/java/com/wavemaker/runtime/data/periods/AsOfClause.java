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
package com.wavemaker.runtime.data.periods;

import java.util.Collections;
import java.util.Date;

import com.wavemaker.runtime.data.annotations.TableTemporal;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.filter.WMQueryParamInfo;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 28/11/17
 */
public class AsOfClause implements PeriodClause {

    private final TableTemporal.TemporalType type;
    private final Date timestamp;

    public AsOfClause(final TableTemporal.TemporalType type, final Date timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    @Override
    public WMQueryInfo asWMQueryClause() {
        String variableName = "wm_" + type.asHqlKeyword() + "_as_of_timestamp";
        final String hql = type.asHqlKeyword()
            + " as of :" + variableName;
        return new WMQueryInfo(hql, Collections.singletonMap(variableName, new WMQueryParamInfo(timestamp)));
    }
}
