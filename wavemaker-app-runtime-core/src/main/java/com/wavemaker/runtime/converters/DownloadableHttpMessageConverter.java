/**
 * Copyright © 2013 - 2017 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.converters;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpResponse;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.file.model.DownloadResponse;
import com.wavemaker.runtime.file.model.Downloadable;

/**
 * @Author: sowmyad
 */
public class DownloadableHttpMessageConverter extends WMCustomAbstractHttpMessageConverter<Downloadable> {

    public DownloadableHttpMessageConverter() {
        super(MediaType.ALL);
    }
    @Override
    protected boolean supports(Class<?> clazz) {
       return Downloadable.class.isAssignableFrom(clazz);
    }

    @Override
    protected DownloadResponse readInternal(Class<? extends Downloadable> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
       throw new WMRuntimeException("Does not support DownloadResponse de-serialization");
    }

    @Override
    protected void writeInternal(Downloadable downloadable, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        ServletServerHttpResponse servletServerHttpResponse = (ServletServerHttpResponse) outputMessage;
        HttpServletResponse httpServletResponse = servletServerHttpResponse.getServletResponse();
        writeMessage(downloadable, httpServletResponse);
    }

    public void writeMessage(Downloadable downloadable, HttpServletResponse httpServletResponse) throws IOException {
        InputStream contents = null;
        try {
            contents = downloadable.getContents();
            if (contents != null) {
                String fileName = downloadable.getFileName();
                String contentType = StringUtils.isNotBlank(downloadable.getContentType()) ? downloadable.getContentType() : new Tika().detect(fileName);
                if (downloadable.isInline()) {
                    httpServletResponse.setHeader("Content-Disposition", "inline;filename=\"" + fileName + "\"");
                } else {
                    httpServletResponse.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
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

