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

package com.wavemaker.runtime.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import com.wavemaker.runtime.web.SkipEtagHttpServletResponseWrapper;

class CSPResponseWrapper extends SkipEtagHttpServletResponseWrapper {

    private ByteArrayOutputStream byteArrayOutputStream;
    private PrintWriter printWriter;
    private ServletOutputStream servletOutputStream;

    public CSPResponseWrapper(HttpServletResponse response) {
        super(response);
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (printWriter != null) {
            throw new IllegalStateException("getWriter() already called");
        }
        if (this.servletOutputStream == null) {
            servletOutputStream = new CSPServletOutputStream(byteArrayOutputStream);
        }
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (servletOutputStream != null)
            throw new IllegalStateException("OutputStream already called -- cannot return PrintWriter");
        if (printWriter == null) {
            printWriter = new PrintWriter(byteArrayOutputStream);
        }
        return printWriter;
    }

    public byte[] getByteArray() {
        return byteArrayOutputStream.toByteArray();
    }
}
