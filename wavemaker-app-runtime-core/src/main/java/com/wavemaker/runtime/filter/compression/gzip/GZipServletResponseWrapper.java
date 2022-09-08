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
package com.wavemaker.runtime.filter.compression.gzip;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.util.MimeType;

import com.wavemaker.runtime.filter.compression.CompressionFilterConfig;

/**
 * @author Kishore Routhu on 10/10/17 6:54 PM.
 */
public class GZipServletResponseWrapper extends HttpServletResponseWrapper {
    private ServletOutputStream outputStream;
    private PrintWriter printWriter;
    private CompressionFilterConfig filterConfig;
    private boolean compressionEnabled = true;
    private long originalContentLength;

    public GZipServletResponseWrapper(CompressionFilterConfig filterConfig, HttpServletResponse response) throws IOException {
        super(response);
        this.filterConfig = filterConfig;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public long getOriginalContentLength() {
        return originalContentLength;
    }

    public void close() throws IOException {

        if (this.printWriter != null) {
            this.printWriter.close();
        }

        if (this.outputStream != null) {
            this.outputStream.close();
        }
    }

    /**
     * Flush OutputStream or PrintWriter
     */

    @Override
    public void flushBuffer() throws IOException {

        if (this.printWriter != null) {
            this.printWriter.flush();
        }

        IOException exception1 = null;
        try {
            if (this.outputStream != null) {
                this.outputStream.flush();
            }
        } catch (IOException e) {
            exception1 = e;
        }

        IOException exception2 = null;
        try {
            super.flushBuffer();
        } catch (IOException e) {
            exception2 = e;
        }

        if (exception1 != null) {
            throw exception1;
        }
        if (exception2 != null) {
            throw exception2;
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.printWriter != null) {
            throw new IllegalStateException("PrintWriter obtained already - cannot get OutputStream");
        }

        if (this.outputStream == null) {
            this.outputStream = new GZipServletOutputStreamWrapper(this, getResponse().getOutputStream());
        }
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.printWriter == null && this.outputStream != null) {
            throw new IllegalStateException("OutputStream obtained already - cannot get PrintWriter");
        }
        if (this.printWriter == null) {
            this.outputStream = new GZipServletOutputStreamWrapper(this, getResponse().getOutputStream());
            this.printWriter = new PrintWriter(new OutputStreamWriter(this.outputStream, getResponse().getCharacterEncoding()));
        }
        return this.printWriter;
    }

    @Override
    public void setContentLength(int len) {
        setContentLengthLong(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        this.compressionEnabled = compressionEnabled && (len >= filterConfig.getMinCompressSize());
        this.originalContentLength = len;
    }

    @Override
    public void setContentType(String contentType) {
        this.compressionEnabled = compressionEnabled && isContentTypeMatched(contentType);
        super.setContentType(contentType);
    }

    @Override
    public void addHeader(String name, String value) {
        if (name.equals(HttpHeaders.CONTENT_TYPE)) {
            setContentType(value);
        } else if (name.equals(HttpHeaders.CONTENT_LENGTH)) {
            setContentLength(Integer.parseInt(value));
        } else {
            super.addHeader(name, value);
        }
    }

    private boolean isContentTypeMatched(String contentType) {
        MimeType requestedMimeType = MimeType.valueOf(contentType);

        String excludeMimeTypes = filterConfig.getExcludeMimeTypes();
        if (StringUtils.isNotBlank(excludeMimeTypes) && isMimeTypeCompatible(requestedMimeType, excludeMimeTypes)) {
            return false;
        }

        String includeMimeTypes = filterConfig.getIncludeMimeTypes();
        if (StringUtils.isNotBlank(includeMimeTypes) && isMimeTypeCompatible(requestedMimeType, includeMimeTypes)) {
            return true;
        }

        return false;
    }

    private boolean isMimeTypeCompatible(MimeType requestedMimeType, String configuredMimeTypes) {
        return Arrays.stream(configuredMimeTypes.split(","))
            .map(MimeType::valueOf)
            .filter(mimeType -> requestedMimeType.isCompatibleWith(mimeType))
            .findFirst()
            .isPresent();
    }
}
