package com.wavemaker.runtime.web.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class CDNUrlReplacementServletOutputStreamWrapper extends ServletOutputStream {

    private ByteArrayOutputStream out;

    public CDNUrlReplacementServletOutputStreamWrapper(ByteArrayOutputStream out) {
        super();
        this.out = out;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
    }
}