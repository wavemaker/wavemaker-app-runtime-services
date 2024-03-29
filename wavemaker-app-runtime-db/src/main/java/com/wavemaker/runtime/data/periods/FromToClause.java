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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.wavemaker.runtime.data.annotations.TableTemporal;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.filter.WMQueryParamInfo;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 29/11/17
 */
public class FromToClause implements PeriodClause {

    private final TableTemporal.TemporalType type;
    private final Date from;
    private final Date to;

    public FromToClause(final TableTemporal.TemporalType type, final Date from, final Date to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    @Override
    public WMQueryInfo asWMQueryClause() {
        String var1Name = "wm_" + type.asHqlKeyword() + "_from_timestamp";
        String var2Name = "wm_" + type.asHqlKeyword() + "_to_timestamp";
        String hql = type.asHqlKeyword() + " from :" + var1Name + " to :" + var2Name;

        Map<String, WMQueryParamInfo> parameters = new HashMap<>(2);
        parameters.put(var1Name, new WMQueryParamInfo(from));
        parameters.put(var2Name, new WMQueryParamInfo(to));

        return new WMQueryInfo(hql, parameters);
    }
}
