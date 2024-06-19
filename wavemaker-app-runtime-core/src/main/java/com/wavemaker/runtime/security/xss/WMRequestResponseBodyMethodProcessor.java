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

package com.wavemaker.runtime.security.xss;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.wavemaker.commons.processor.ChildObjectRetriever;
import com.wavemaker.commons.processor.DefaultChildObjectRetriever;
import com.wavemaker.commons.processor.ObjectProcessor;
import com.wavemaker.commons.processor.ProcessContext;
import com.wavemaker.commons.processor.RecursiveObjectProcessor;

public class WMRequestResponseBodyMethodProcessor extends RequestResponseBodyMethodProcessor {

    private static final List<Class<?>> EXCLUDED_CLASSES = Arrays.asList(HttpServletRequest.class,
        HttpServletResponse.class, Pageable.class);

    public WMRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        Method method = returnType.getMethod();

        boolean xssDisable = Arrays.stream(Objects.requireNonNull(method).getDeclaringClass().getDeclaredAnnotations())
            .anyMatch(XssDisable.class::isInstance) || Arrays.stream(method.getDeclaredAnnotations())
            .anyMatch(XssDisable.class::isInstance);

        if (!xssDisable) {
            returnValue = getSanitizedObject(returnValue, DataFlowType.OUTGOING);
        }
        super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object o = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        boolean xssDisable = Arrays.stream(Objects.requireNonNull(parameter.getMethod()).getDeclaringClass().getDeclaredAnnotations())
            .anyMatch(XssDisable.class::isInstance) || Arrays.stream(Objects.requireNonNull(parameter.getMethod()).getDeclaredAnnotations())
            .anyMatch(XssDisable.class::isInstance);
        if (!xssDisable) {
            return getSanitizedObject(o, DataFlowType.INCOMING);
        }
        return o;
    }

    private Object getSanitizedObject(Object object, DataFlowType dataFlowType) {
        ChildObjectRetriever childObjectRetriever = new DefaultChildObjectRetriever(this::isXssEnabledForField);
        ObjectProcessor xssEncodeProcessor = new XssEncodeProcessor(dataFlowType);
        RecursiveObjectProcessor recursiveObjectProcessor =
            new RecursiveObjectProcessor(xssEncodeProcessor, childObjectRetriever, new ProcessContext(), this::isXssEnabled);
        return recursiveObjectProcessor.processRootObject(object);
    }

    private boolean isXssEnabled(Object object) {
        return EXCLUDED_CLASSES.stream().noneMatch(type -> type.isInstance(object)) && !object.getClass().isAnnotationPresent(XssDisable.class);
    }

    private boolean isXssEnabledForField(Field field) {
        return !field.isAnnotationPresent(XssDisable.class);
    }
}
