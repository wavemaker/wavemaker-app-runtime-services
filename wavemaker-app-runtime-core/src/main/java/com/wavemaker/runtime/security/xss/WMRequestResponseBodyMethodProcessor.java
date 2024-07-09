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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class WMRequestResponseBodyMethodProcessor extends RequestResponseBodyMethodProcessor {

    public WMRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        try {
            Method method = returnType.getMethod();
            boolean xssDisable = Arrays.stream(Objects.requireNonNull(method).getDeclaringClass().getDeclaredAnnotations())
                .anyMatch(XssDisable.class::isInstance) || Arrays.stream(method.getDeclaredAnnotations())
                .anyMatch(XssDisable.class::isInstance);
            XssContext.setXssEnabled(!xssDisable);
            if (!xssDisable && returnValue instanceof String) {
                //Handling encoding if returnValue is string as StringHttpMessageConverter is invoked for strings instead of XssStringSerializer
                returnValue = XSSSecurityHandler.getInstance().sanitizeOutgoingData((String) returnValue);
            }
            super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
        } finally {
            XssContext.cleanup();
        }
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        try {
            boolean xssDisable = Arrays.stream(Objects.requireNonNull(parameter.getMethod()).getDeclaringClass().getDeclaredAnnotations())
                .anyMatch(XssDisable.class::isInstance) || Arrays.stream(Objects.requireNonNull(parameter.getMethod()).getDeclaredAnnotations())
                .anyMatch(XssDisable.class::isInstance);
            XssContext.setXssEnabled(!xssDisable);
            return super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        } finally {
            XssContext.cleanup();
        }
    }
}
