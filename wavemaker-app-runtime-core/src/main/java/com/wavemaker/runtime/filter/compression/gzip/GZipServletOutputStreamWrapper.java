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
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.springframework.http.HttpHeaders;

/**
 * @author Kishore Routhu on 10/10/17 6:47 PM.
 */
public class GZipServletOutputStreamWrapper extends ServletOutputStream {

    private GZipServletResponseWrapper responseWrapper;
    private OutputStream outputStream;
    private boolean streamInitialized;

    public GZipServletOutputStreamWrapper(GZipServletResponseWrapper responseWrapper, OutputStream outputStream) throws IOException {
        this.responseWrapper = responseWrapper;
        this.outputStream = outputStream;
    }

    @Override
    public void write(int b) throws IOException {
        getOutputStream().write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        getOutputStream().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        getOutputStream().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        getOutputStream().flush();
    }

    @Override
    public void close() throws IOException {
        getOutputStream().close();
    }

    private OutputStream getOutputStream() throws IOException {
        if (!streamInitialized) {
            initStream();
        }
        return outputStream;
    }

    private synchronized void initStream() throws IOException {
        if (responseWrapper.isCompressionEnabled()) {
            responseWrapper.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            outputStream = new GZIPOutputStream(outputStream);
        } else {
            responseWrapper.setContentLengthLong(responseWrapper.getOriginalContentLength());
        }
        streamInitialized = true;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
