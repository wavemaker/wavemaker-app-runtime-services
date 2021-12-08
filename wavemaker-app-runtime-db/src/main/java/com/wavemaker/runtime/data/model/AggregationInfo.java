/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.model;

import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 15/3/17
 */
public class AggregationInfo {

    private List<String> groupByFields;
    private List<Aggregation> aggregations;
    private String filter;

    public List<String> getGroupByFields() {
        return groupByFields;
    }

    public void setGroupByFields(final List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public List<Aggregation> getAggregations() {
        return aggregations;
    }

    public void setAggregations(final List<Aggregation> aggregations) {
        this.aggregations = aggregations;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AggregationInfo)) {
            return false;
        }
        final AggregationInfo that = (AggregationInfo) o;
        return Objects.equals(getGroupByFields(), that.getGroupByFields()) &&
                Objects.equals(getAggregations(), that.getAggregations()) &&
                Objects.equals(getFilter(), that.getFilter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupByFields(), getAggregations(), getFilter());
    }

    @Override
    public String toString() {
        return "AggregationInfo{" +
                "groupByFields=" + groupByFields +
                ", aggregations=" + aggregations +
                ", filter='" + filter + '\'' +
                '}';
    }
}
