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
package com.wavemaker.runtime.data.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wavemaker.runtime.data.dao.procedure.parameters.ResolvableParam;
import com.wavemaker.runtime.data.dao.procedure.parameters.TestParameter;
import com.wavemaker.runtime.data.dao.util.QueryHelper;
import com.wavemaker.runtime.data.model.ReferenceType;
import com.wavemaker.runtime.data.model.procedures.ProcedureParameter;
import com.wavemaker.runtime.data.model.procedures.RuntimeProcedure;
import com.wavemaker.runtime.data.model.queries.QueryType;
import com.wavemaker.runtime.data.model.queries.RuntimeQuery;
import com.wavemaker.runtime.data.model.returns.FieldType;
import com.wavemaker.runtime.data.model.returns.ReturnProperty;
import com.wavemaker.runtime.data.util.DataServiceUtils;

/**
 * @author <a href="mailto:anusha.dharmasagar@wavemaker.com">Anusha Dharmasagar</a>
 * @since 27/1/17
 */
public class DesignTimeServiceUtils {

    private DesignTimeServiceUtils() {
    }

    public static boolean isDMLOrUpdateQuery(RuntimeQuery query) {
        return query.getType() != QueryType.SELECT && DataServiceUtils.isDML(query.getQueryString());
    }

    public static List<ReturnProperty> getMetaForDML() {
        return Collections.singletonList(new ReturnProperty(null, new FieldType(ReferenceType.PRIMITIVE, Integer
            .class.getName())));
    }

    public static List<ResolvableParam> prepareParameters(final RuntimeProcedure procedure) {
        List<ResolvableParam> testParameters = new ArrayList<>(procedure.getParameters().size());

        final List<ProcedureParameter> parameters = QueryHelper.prepareProcedureParameters(procedure);
        for (final ProcedureParameter parameter : parameters) {
            testParameters.add(new TestParameter(parameter));
        }
        return testParameters;
    }
}
