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

import java.sql.Timestamp;

import org.springframework.util.Assert;

import com.wavemaker.commons.InvalidInputException;
import com.wavemaker.runtime.data.annotations.TableTemporal;
import com.wavemaker.runtime.data.filter.WMQueryInfo;
import com.wavemaker.runtime.data.model.JavaType;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 6/12/17
 */
public class PeriodClauseWrapper implements PeriodClause {

    private static final String ERROR_MESSAGE = "period clause should be as_of <arg>, from_to <arg1> <arg2>, between " +
        "<arg1> <arg2>";

    private PeriodClause periodClause;

    public PeriodClauseWrapper(TableTemporal.TemporalType type, String clause) {
        final String[] tokens = clause.split("\\s");

        final String periodType = tokens[0];
        try {
            if ("as_of".equalsIgnoreCase(periodType)) {
                Assert.isTrue(tokens.length == 2, "As of clause MUST have one argument");
                periodClause = new AsOfClause(type, (Timestamp) JavaType.TIMESTAMP.fromString(tokens[1]));
            } else if ("from_to".equalsIgnoreCase(periodType)) {
                Assert.isTrue(tokens.length == 3, "FromTo clause MUST have two arguments");
                final Timestamp from = (Timestamp) JavaType.TIMESTAMP.fromString(tokens[1]);
                final Timestamp to = (Timestamp) JavaType.TIMESTAMP.fromString(tokens[2]);
                periodClause = new FromToClause(type, from, to);
            } else if ("between".equalsIgnoreCase(periodType)) {
                Assert.isTrue(tokens.length == 3, "Between clause MUST have two arguments");
                final Timestamp from = (Timestamp) JavaType.TIMESTAMP.fromString(tokens[1]);
                final Timestamp to = (Timestamp) JavaType.TIMESTAMP.fromString(tokens[2]);
                periodClause = new BetweenClause(type, from, to);
            } else {
                throw new InvalidInputException(ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(e.getMessage() + ", Syntax:" + ERROR_MESSAGE, e);
        }

    }

    @Override
    public WMQueryInfo asWMQueryClause() {
        return periodClause.asWMQueryClause();
    }
}
