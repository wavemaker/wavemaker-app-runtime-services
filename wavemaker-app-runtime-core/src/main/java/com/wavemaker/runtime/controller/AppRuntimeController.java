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
package com.wavemaker.runtime.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.validations.DbValidationsConstants;
import com.wavemaker.commons.wrapper.StringWrapper;
import com.wavemaker.runtime.commons.file.manager.ExportedFileManager;
import com.wavemaker.runtime.commons.file.model.DownloadResponse;
import com.wavemaker.runtime.commons.file.model.ExportedFileContentWrapper;
import com.wavemaker.runtime.commons.util.FileUploadConstants;
import com.wavemaker.runtime.service.AppRuntimeService;

/**
 * @author Sowmya
 */

@RestController
@RequestMapping("/")
public class AppRuntimeController {

    @Autowired
    private AppRuntimeService appRuntimeService;

    @Autowired
    private ExportedFileManager exportedFileManager;

    @GetMapping(value = "/application/type")
    public StringWrapper getApplicationType() {
        String applicationType = appRuntimeService.getApplicationType();
        return new StringWrapper(applicationType);
    }

    @GetMapping(value = "/application/wmProperties.js")
    public void getApplicationProperties(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/javascript;charset=UTF-8");
        Map<String, Object> applicationProperties = appRuntimeService.getApplicationProperties();
        String acceptLanguageHeader = request.getHeader("Accept-Language");
        if (acceptLanguageHeader != null) {
            applicationProperties.put("preferredLanguage", URLEncoder.encode(acceptLanguageHeader, "UTF-8"));
        }
        Object allowedFileUploadExtensions = applicationProperties.get(FileUploadConstants.ALLOWED_FILE_UPLOAD_EXTENSIONS);
        if (allowedFileUploadExtensions == null) {
            applicationProperties.put(FileUploadConstants.ALLOWED_FILE_UPLOAD_EXTENSIONS, FileUploadConstants.DEFAULT_ALLOWED_FILE_UPLOAD_EXTENSIONS);
        }
        Object enableSkipToMainContent = applicationProperties.get("enableSkipToMainContent");
        if (enableSkipToMainContent == null) {
            applicationProperties.put("enableSkipToMainContent", "false");
        }
        response.getWriter().write("var _WM_APP_PROPERTIES = " + JSONUtils.toJSON(applicationProperties, true) + ";");
        response.getWriter().flush();
    }

    @GetMapping(value = "/application/validations")
    public DownloadResponse getValidations(HttpServletResponse httpServletResponse) {
        InputStream inputStream = appRuntimeService.getValidations(httpServletResponse);
        DownloadResponse downloadResponse = new DownloadResponse(inputStream, MediaType.APPLICATION_JSON_VALUE, DbValidationsConstants.DB_VALIDATIONS_JSON_FILE);
        downloadResponse.setInline(true);
        return downloadResponse;
    }

    @GetMapping(value = "/files/exported/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public DownloadResponse getExportedFile(@PathVariable("fileId") String fileId) {
        ExportedFileContentWrapper fileContents = exportedFileManager.getFileContent(fileId);
        return new DownloadResponse(fileContents.getInputStream(), MediaType.APPLICATION_OCTET_STREAM_VALUE, fileContents.getFileName());
    }
}

