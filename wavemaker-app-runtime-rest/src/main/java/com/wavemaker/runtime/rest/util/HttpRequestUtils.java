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
package com.wavemaker.runtime.rest.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.io.DeleteTempFileOnCloseInputStream;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.runtime.rest.model.HttpResponseDetails;
import com.wavemaker.runtime.rest.model.Message;

/**
 * Created by ArjunSahasranam on 9/6/16.
 */
public class HttpRequestUtils {
    private HttpRequestUtils() {
    }

    public static void writeResponse(HttpResponseDetails httpResponseDetails, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(httpResponseDetails.getStatusCode());
        Map<String, List<String>> responseHeaders = httpResponseDetails.getHeaders();
        for (Map.Entry<String, List<String>> responseHeaderEntry : responseHeaders.entrySet()) {
            for (String responseHeaderValue : responseHeaderEntry.getValue()) {
                httpServletResponse.setHeader(responseHeaderEntry.getKey(), responseHeaderValue);
            }
        }
        InputStream inputStream = httpResponseDetails.getBody();
        if (inputStream != null) {
            try {
                OutputStream outputStream = httpServletResponse.getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    public static Message getFormMessage(Map<String, Object> map) {
        MultiValueMap<String, Object> multiValueMap = getMultiValueMap(map);
        return createMessage(multiValueMap, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    public static Message getMultipartMessage(Map<String, Object> map) {
        MultiValueMap<String, Object> multiValueMap = getMultiValueMap(map);
        return createMessage(multiValueMap, MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    public static Message getJsonMessage(Object body) {
        if (body == null || "".equals(body)) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.empty.object"));
        }
        Message message = new Message();
        message.setHttpHeaders(new HttpHeaders());
        message.setInputStream(IOUtils.toInputStream(JSONUtils.toJSON(body), Charset.defaultCharset()));
        return message;
    }

    public static Message createMessage(MultiValueMap<String, Object> map, String contentType) {
        RestHttpOutputMessage httpOutputMessage = new HttpRequestUtils.RestHttpOutputMessage();
        httpOutputMessage.setHeaders(new HttpHeaders());
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        formHttpMessageConverter.setPartConverters(getPartConverters());
        try {
            File file = File.createTempFile("requestBody", ".tmp");
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))){
                httpOutputMessage.setBody(outputStream);
                formHttpMessageConverter.write(map, MediaType.valueOf(contentType), httpOutputMessage);
            }
            Message message = new Message();
            message.setHttpHeaders(httpOutputMessage.getHeaders());
            message.setInputStream(new DeleteTempFileOnCloseInputStream(file));
            return message;
        } catch (Exception e) {
            throw new WMRuntimeException("Failed to create message body", e);
        }
    }

    private static List<HttpMessageConverter<?>> getPartConverters(){
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter() {

            @Override
            protected MediaType getDefaultContentType(String t) throws IOException {
                return null;
            }
        };
        messageConverters.add(stringHttpMessageConverter);
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new WmFileSystemResourceConverter());
        return messageConverters;
    }


    private static MultiValueMap<String, Object> getMultiValueMap(Map<String, Object> map) {
        MultiValueMap<String, Object> multiValueMap;
        if (map instanceof MultiValueMap) {
            multiValueMap = (MultiValueMap) map;
        } else {
            multiValueMap = new LinkedMultiValueMap();
            multiValueMap.setAll(map);
        }
        return multiValueMap;
    }

    private static class RestHttpOutputMessage implements HttpOutputMessage {

        private OutputStream body;
        private HttpHeaders headers;

        @Override
        public OutputStream getBody() throws IOException {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        public void setBody(OutputStream body) {
            this.body = body;
        }

        public void setHeaders(HttpHeaders httpHeaders) {
            this.headers = httpHeaders;
        }
    }

}
