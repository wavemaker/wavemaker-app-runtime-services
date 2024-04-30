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
package com.wavemaker.runtime.web.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wavemaker.commons.util.FileValidationUtils;
import com.wavemaker.commons.util.HttpRequestUtils;
import com.wavemaker.commons.util.WMIOUtils;

/**
 * Created by kishore on 24/3/17.
 */
public class PrefabWebContentServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrefabWebContentServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String prefabResourcePath = request.getPathInfo();
        if (StringUtils.isBlank(prefabResourcePath)) {
            writeErrorResponse(request, response, prefabResourcePath);
            return;
        }

        if (prefabResourcePath.startsWith("/")) {
            prefabResourcePath = prefabResourcePath.substring(1);
        }

        int endIndex = prefabResourcePath.indexOf('/');
        if (endIndex == -1) {
            writeErrorResponse(request, response, prefabResourcePath);
            return;
        }

        String prefabName = prefabResourcePath.substring(0, endIndex);
        String resourceRelativePath = prefabResourcePath.substring(prefabName.length());
        String prefabResourceUpdatedPath = "/WEB-INF/prefabs/" + prefabName + "/webapp" + resourceRelativePath;
        String contentType = new Tika().detect(resourceRelativePath);
        if (contentType != null) {
            response.setContentType(contentType);
        }

        InputStream inputStream = null;
        try {
            inputStream = getServletContext().getResourceAsStream(FileValidationUtils.validateFilePath(prefabResourceUpdatedPath));
            if (inputStream == null) {
                HttpRequestUtils.writeJsonErrorResponse("Resource not found", HttpStatus.SC_NOT_FOUND, response);
                return;
            }
            ServletOutputStream outputStream = response.getOutputStream();
            WMIOUtils.copy(inputStream, outputStream);
        } finally {
            WMIOUtils.closeSilently(inputStream);
        }
    }

    private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, String prefabResourcePath) throws IOException {
        LOGGER.warn("Invalid prefab uri {} received", request.getRequestURI());
        HttpRequestUtils.writeJsonErrorResponse("Invalid prefab url " + request.getRequestURI(), HttpStatus.SC_BAD_REQUEST, response);
    }
}
