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
package com.wavemaker.runtime.data.export;


import java.util.ArrayList;
import java.util.List;

public class ExportOptions {
    private List<FieldInfo> fields = new ArrayList<>();
    private ExportType exportType;
    private Integer exportSize = Integer.MAX_VALUE;
    private String fileName;

    public ExportOptions(ExportType exportType) {
        this.exportType = exportType;
    }

    public ExportOptions(final ExportType exportType, final Integer exportSize) {
        this.exportType = exportType;
        this.exportSize = exportSize;
    }

    public ExportOptions() {
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public void setExportType(ExportType exportType) {
        this.exportType = exportType;
    }

    public Integer getExportSize() {
        return exportSize;
    }

    public void setExportSize(final Integer exportSize) {
        this.exportSize = exportSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }
}
