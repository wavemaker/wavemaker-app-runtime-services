package com.wavemaker.runtime.converters;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class WMYamlJackson2HttpMessageConverter extends WMCustomAbstractHttpMessageConverter<Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public WMYamlJackson2HttpMessageConverter() {
        super(new MediaType("application", "x-yaml"), new MediaType("text", "x-yaml"), new MediaType("text", "yaml"),
                new MediaType("application", "yml"), new MediaType("text", "yml"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Map<String, Object> readInternal(Class<? extends Map<String, Object>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return objectMapper.readValue(inputMessage.getBody(), Map.class);
    }

    @Override
    protected void writeInternal(Map<String, Object> map, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody());
        objectMapper.writeValue(writer, map);
        writer.close();
    }
}