package com.wavemaker.runtime.converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.wavemaker.studio.common.WMRuntimeException;

/**
 * @Author: Uday
 */
public class WMCompositeHttpMessageConverter<T> implements HttpMessageConverter<T> {

    private List<WMCustomAbstractHttpMessageConverter> httpMessageConverterList = new ArrayList<>();

    private List<MediaType> supportedMediaTypes = new ArrayList<>();

    public WMCompositeHttpMessageConverter() {
        this.supportedMediaTypes.add(MediaType.ALL);
        this.httpMessageConverterList.add(new DownloadableHttpMessageConverter());

    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        for (HttpMessageConverter httpMessageConverter : httpMessageConverterList) {
            if (httpMessageConverter.canRead(clazz, mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        for (HttpMessageConverter httpMessageConverter : httpMessageConverterList) {
            if (httpMessageConverter.canWrite(clazz, mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return supportedMediaTypes;
    }

    @Override
    public T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        for (WMCustomHttpMessageConverter httpMessageConverter : httpMessageConverterList) {
            if (httpMessageConverter.supportsClazz(clazz)) {
                return (T) httpMessageConverter.read(clazz, inputMessage);
            }
        }
        throw new WMRuntimeException("Cannot read the object of type " + clazz.getName());
    }

    @Override
    public void write(T t, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        for (WMCustomHttpMessageConverter httpMessageConverter : httpMessageConverterList) {
            if (httpMessageConverter.supportsClazz(t.getClass())) {
                httpMessageConverter.write(t, null, outputMessage);
                return;
            }
        }
        throw new WMRuntimeException("Cannot write the object of type " + t.getClass().getName());
    }
}

