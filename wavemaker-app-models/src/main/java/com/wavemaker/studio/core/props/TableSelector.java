/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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
package com.wavemaker.studio.core.props;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 15/9/16
 */
public class TableSelector {

    private String table;
    private String schema;

    public TableSelector() {
    }

    public TableSelector(final String table, final String schema) {
        this.table = table;
        this.schema = schema;
    }

    public TableSelector(final TableSelector other) {
        this.schema = other.schema;
        this.table = other.table;
    }

    public String getSchema() {
        return schema;
    }

    public TableSelector setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public TableSelector setTable(String table) {
        this.table = table;
        return this;
    }

    public String getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TableSelector that = (TableSelector) o;

        return getTable() != null ? getTable().equals(that.getTable()) : that.getTable() == null;

    }

    @Override
    public int hashCode() {
        return getTable() != null ? getTable().hashCode() : 0;
    }

    public String toString() {
        return "TableSelector{" +
            "schema='" + schema + '\'' +
            ", table='" + table + '\'' +
            '}';
    }
}
