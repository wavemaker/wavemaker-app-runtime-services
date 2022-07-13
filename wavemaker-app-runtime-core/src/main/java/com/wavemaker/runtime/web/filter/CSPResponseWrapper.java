package com.wavemaker.runtime.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class CSPResponseWrapper extends HttpServletResponseWrapper {

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
