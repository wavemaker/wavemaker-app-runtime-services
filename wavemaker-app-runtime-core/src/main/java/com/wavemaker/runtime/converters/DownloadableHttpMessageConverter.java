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
package com.wavemaker.runtime.converters;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.commons.converters.WMCustomAbstractHttpMessageConverter;
import com.wavemaker.runtime.commons.file.model.DownloadResponse;
import com.wavemaker.runtime.commons.file.model.Downloadable;

/**
 *
 */
public class DownloadableHttpMessageConverter extends WMCustomAbstractHttpMessageConverter<Downloadable> {

    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    public DownloadableHttpMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Downloadable.class.isAssignableFrom(clazz);
    }

    @Override
    protected DownloadResponse readInternal(
        Class<? extends Downloadable> clazz, HttpInputMessage inputMessage) throws IOException {
        throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.unsupported.de-serialization"));
    }

    @Override
    protected void writeInternal(Downloadable downloadable, HttpOutputMessage outputMessage) throws IOException {
        ServletServerHttpResponse servletServerHttpResponse = (ServletServerHttpResponse) outputMessage;
        outputMessage.getHeaders().remove(CONTENT_DISPOSITION_HEADER); // removing if any
        HttpServletResponse httpServletResponse = servletServerHttpResponse.getServletResponse();
        writeMessage(downloadable, httpServletResponse);
    }

    public void writeMessage(Downloadable downloadable, HttpServletResponse httpServletResponse) throws IOException {
        InputStream contents = null;
        try {
            contents = downloadable.getContents();
            if (contents != null) {
                String fileName = downloadable.getFileName();
                String contentType = StringUtils.isNotBlank(downloadable.getContentType()) ? downloadable
                    .getContentType() : new Tika().detect(fileName);
                if (downloadable.isInline()) {
                    httpServletResponse.setHeader(CONTENT_DISPOSITION_HEADER, "inline;filename=\"" + fileName + "\"");
                } else {
                    httpServletResponse.setHeader(CONTENT_DISPOSITION_HEADER, "attachment;filename=\"" + fileName + "\"");
                }
                httpServletResponse.setContentType(contentType);
                if (downloadable.getContentLength() != null) {
                    httpServletResponse.setContentLength(downloadable.getContentLength());
                }

                WMIOUtils.copy(contents, httpServletResponse.getOutputStream());
            }
        } finally {
            WMIOUtils.closeSilently(contents);
        }
    }
}

