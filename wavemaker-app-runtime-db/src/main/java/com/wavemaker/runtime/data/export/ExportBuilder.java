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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.io.File;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.data.export.util.CSVConverterUtil;
import com.wavemaker.runtime.data.export.util.DataSourceExporterUtil;

/**
 * @author <a href="mailto:anusha.dharmasagar@wavemaker.com">Anusha Dharmasagar</a>
 * @since 16/11/16
 */
public class ExportBuilder {

    private static final int FIRST_ROW_NUMBER = 0;
    private static final int FIRST_COLUMN_NUMBER = 0;

    private QueryExtractor queryExtractor;
    private ExportOptionsStrategy optionsStrategy;
    private ExportOptions options;

    private ExportCellStyles cellStyles; // todo should have separate class for context.

    public ExportBuilder(final QueryExtractor queryExtractor, ExportOptions options, final Class<?> entityClass) {
        this.queryExtractor = queryExtractor;
        this.options = options;
        optionsStrategy = new ExportOptionsStrategy(options, entityClass);
    }

    public void build(OutputStream outputStream) {
        SXSSFWorkbook workbook = null;
        try {
            workbook = new SXSSFWorkbook();
            cellStyles = new ExportCellStyles(workbook);
            SXSSFSheet spreadSheet = workbook.createSheet("Data");
            fillSheet(spreadSheet);
            exportWorkbook(workbook, options.getExportType(), outputStream);
        } catch (Exception e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.export.building.error"), e);
        } finally {
            if (workbook != null) {
                workbook.dispose();
            }
            WMIOUtils.closeSilently(workbook);
        }
    }

    private void exportWorkbook(final Workbook workbook, final ExportType exportType, OutputStream outputStream) {
        try {
            if (exportType == ExportType.EXCEL) {
                workbook.write(outputStream);
            } else if (exportType == ExportType.CSV) {
                // to support larger data files we are using SXSSFWorkbook
                final File tempFile = WMIOUtils.createTempFile("export", "excel_csv");
                try {
                    try (OutputStream tos = tempFile.getContent().asOutputStream()) {
                        workbook.write(tos);
                    }
                    try (InputStream inputStream = tempFile.getContent().asInputStream();
                         Workbook tempWorkbook = WorkbookFactory.create(inputStream)) {
                        CSVConverterUtil csvConverterUtil = new CSVConverterUtil(tempWorkbook);
                        csvConverterUtil.convert(outputStream);
                    }
                } finally {
                    tempFile.delete();
                }

            }
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.data.exporting.error"), e);
        }
    }

    private void fillSheet(Sheet sheet) throws Exception {
        int rowNum = FIRST_ROW_NUMBER;
        fillHeader(sheet.createRow(rowNum++), optionsStrategy.getDisplayNames(), sheet);
        while (queryExtractor.next()) {
            Row row = sheet.createRow(rowNum);
            final Object dataObject = queryExtractor.getCurrentRow();
            fillRow(dataObject, row);
            rowNum++;
        }
    }

    private void fillRow(Object rowData, Row row) throws Exception {
        if (rowData == null) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.report.generation.failed"));
        }
        fillData(optionsStrategy.readRowData(rowData), row);
    }

    private void fillHeader(Row row, List<String> fieldNames, Sheet sheet) {
        int colNum = FIRST_COLUMN_NUMBER;
        for (final String fieldName : fieldNames) {
            CellUtil.createCell(row, colNum, fieldName, cellStyles.getHeaderCellStyle());
            sheet.setColumnWidth(colNum, 20 * 256);
            colNum++;
        }
    }

    private void fillData(List<Object> rowValues, Row row) {
        int colNum = FIRST_COLUMN_NUMBER;
        for (Object value : rowValues) {
            final Cell cell = row.createCell(colNum);
            DataSourceExporterUtil.setCellValue(value, cell, cellStyles);
            colNum++;
        }
    }

}
