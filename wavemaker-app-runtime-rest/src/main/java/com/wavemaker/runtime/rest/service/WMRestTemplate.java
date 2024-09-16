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
package com.wavemaker.runtime.rest.service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.feed.RssChannelHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import org.springframework.web.util.UriTemplateHandler;

import com.wavemaker.runtime.commons.converters.WMYamlJackson2HttpMessageConverter;

/**
 * @author Uday Shankar
 */
public class WMRestTemplate extends RestTemplate {

    private static final boolean ROME_PRESENT =
        ClassUtils.isPresent("com.rometools.rome.feed.WireFeed", WMRestTemplate.class.getClassLoader());

    private static final boolean JAXB_2_PRESENT =
        ClassUtils.isPresent("jakarta.xml.bind.Binder", WMRestTemplate.class.getClassLoader());

    private static final boolean JACKSON_2_PRESENT =
        ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", WMRestTemplate.class.getClassLoader()) &&
            ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", WMRestTemplate.class.getClassLoader());

    private static final boolean JACKSON_2_XML_PRESENT =
        ClassUtils.isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper", WMRestTemplate.class.getClassLoader());

    private static final boolean GSON_PRESENT =
        ClassUtils.isPresent("com.google.gson.Gson", WMRestTemplate.class.getClassLoader());

    private static final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

    static {
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<>());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new WMYamlJackson2HttpMessageConverter());
        if (ROME_PRESENT) {
            messageConverters.add(new AtomFeedHttpMessageConverter());
            messageConverters.add(new RssChannelHttpMessageConverter());
        }
        if (JACKSON_2_XML_PRESENT) {
            messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
        }
        if (JAXB_2_PRESENT) {
            messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
        }
        if (JACKSON_2_PRESENT) {
            messageConverters.add(new MappingJackson2HttpMessageConverter());
        } else if (GSON_PRESENT) {
            messageConverters.add(new GsonHttpMessageConverter());
        }
    }

    private Consumer<ClientHttpResponse> extractDataConsumer;

    public WMRestTemplate() {
        super(messageConverters);
    }

    public RequestCallback getRequestEntityCallBack(Object requestBody) {
        return httpEntityCallback(requestBody);
    }

    public RequestCallback getRequestEntityCallBack(Object requestBody, Type responseType) {
        return httpEntityCallback(requestBody, responseType);
    }

    public <T> ResponseExtractor<ResponseEntity<T>> getResponseEntityExtractor(Type responseType) {
        return responseEntityExtractor(responseType);
    }

    public void setExtractDataConsumer(Consumer<ClientHttpResponse> extractDataConsumer) {
        this.extractDataConsumer = extractDataConsumer;
    }

    @Override
    public <T> ResponseExtractor<ResponseEntity<T>> responseEntityExtractor(Type responseType) {
        return new WmResponseEntityResponseExtractor<>(responseType);
    }

    @Override
    public UriTemplateHandler getUriTemplateHandler() {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(EncodingMode.NONE);
        return uriBuilderFactory;
    }

    /**
     * Response extractor for {@link HttpEntity}.
     */
    private class WmResponseEntityResponseExtractor<T> implements ResponseExtractor<ResponseEntity<T>> {

        @Nullable
        private final HttpMessageConverterExtractor<T> delegate;

        public WmResponseEntityResponseExtractor(@Nullable Type responseType) {
            if (responseType != null && Void.class != responseType) {
                this.delegate = new HttpMessageConverterExtractor<>(responseType, getMessageConverters());
            } else {
                this.delegate = null;
            }
        }

        @Override
        public ResponseEntity<T> extractData(ClientHttpResponse response) throws IOException {
            if (extractDataConsumer != null) {
                response = new WmBufferingClientHttpResponseWrapper(response);
                extractDataConsumer.accept(response);
            }
            if (this.delegate != null) {
                T body = this.delegate.extractData(response);
                return ResponseEntity.status(response.getStatusCode().value()).headers(response.getHeaders()).body(body);
            } else {
                return ResponseEntity.status(response.getStatusCode().value()).headers(response.getHeaders()).build();
            }
        }
    }

}
