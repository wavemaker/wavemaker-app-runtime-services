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

import java.awt.print.Pageable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class WMRequestResponseBodyMethodProcessor extends RequestResponseBodyMethodProcessor {

    private static final List<Class<?>> EXCLUDED_CLASSES = Arrays.asList(HttpServletRequest.class,
        HttpServletResponse.class, Pageable.class);

    public WMRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        Method method = returnType.getMethod();
        boolean xssDisable;

        xssDisable = Arrays.stream(Objects.requireNonNull(method).getDeclaringClass().getDeclaredAnnotations())
            .anyMatch(an -> an instanceof XssDisable) || Arrays.stream(method.getDeclaredAnnotations())
            .anyMatch(an -> an instanceof XssDisable);

        if (!xssDisable) {
            ResponseTuple encodeResult = encode(returnValue, new ArrayList<>(), DataFlowType.OUTGOING);
            if (encodeResult.modified) {
                returnValue = encodeResult.value;
            }
        }
        super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object o = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        boolean xssDisable;
        xssDisable = Arrays.stream(Objects.requireNonNull(parameter.getMethod()).getDeclaringClass().getDeclaredAnnotations())
            .anyMatch(an -> an instanceof XssDisable) || Arrays.stream(parameter.getMethod().getDeclaredAnnotations())
            .anyMatch(an -> an instanceof XssDisable);
        if (!xssDisable) {
            ResponseTuple responseTuple = encode(o, new ArrayList(), DataFlowType.INCOMING);
            return responseTuple.value;
        }
        return o;
    }

    private ResponseTuple encode(Object value, List<Object> manipulatedObjects, DataFlowType dataFlowType) {
        Object encoded = value;
        boolean modified = false;

        if (value != null && !(value instanceof Number) && !(containsInList(value, manipulatedObjects) || isExcludedClass(value))) {
            final Class<?> valueClass = value.getClass();

            if (valueClass == char[].class) {
                encoded = encode(((char[]) value), dataFlowType);
                modified = true;
            } else if (valueClass.isArray()) {
                modified = encodeArray((Object[]) value, manipulatedObjects, dataFlowType);
            } else if (value instanceof String) {
                encoded = encode((String) value, dataFlowType);
                modified = true;
            } else {
                manipulatedObjects.add(value);
                final ResponseTuple response = encodeCustomClass(value, manipulatedObjects, dataFlowType);
                if (response.modified) {
                    encoded = response.value;
                    modified = true;
                }
            }
        }

        return new ResponseTuple(encoded, modified);
    }

    private boolean encodeArray(Object[] valueArray, final List<Object> manipulatedObjects, DataFlowType dataFlowType) {
        boolean modified = false;
        for (int i = 0; i < valueArray.length; i++) {
            Object obj = valueArray[i];
            ResponseTuple result = encode(obj, manipulatedObjects, dataFlowType);
            if (result.modified) {
                valueArray[i] = result.value;
                modified = true;
            }
        }
        return modified;
    }

    private ResponseTuple encodeCustomClass(Object object, List<Object> manipulatedObjects, DataFlowType dataFlowType) {
        AtomicBoolean modified = new AtomicBoolean(false);
        if (object instanceof Page) {
            ((Page) object).getContent().forEach(obj -> {
                obj = encode(obj, manipulatedObjects, dataFlowType).value;
                modified.set(true);
            });
        }
        for (final Field field : object.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(XssDisable.class) && !Modifier.isFinal(field.getModifiers())) {
                ReflectionUtils.makeAccessible(field);
                try {
                    final ResponseTuple response = encode(field.get(object), manipulatedObjects, dataFlowType);
                    if (response.modified) {
                        modified.set(true);
                        field.set(object, response.value);
                    }
                } catch (IllegalAccessException e) {
                    throw new WMRuntimeException(MessageResource.create("reflection.field.error"), e, field.getName(),
                        field.getDeclaringClass().getName());
                }
            }
        }
        return new ResponseTuple(object, modified.get());
    }

    private char[] encode(char[] value, DataFlowType dataFlowType) {
        return encode(new String(value), dataFlowType).toCharArray();
    }

    private String encode(String value, DataFlowType dataFlowType) {
        switch (dataFlowType) {
            case OUTGOING:
                return XSSSecurityHandler.getInstance().sanitizeOutgoingData(value);
            case INCOMING:
                return XSSSecurityHandler.getInstance().sanitizeIncomingData(value);
            default:
                throw new IllegalStateException();
        }
    }

    private boolean isExcludedClass(Object value) {
        Class<?> valueClass = value.getClass();
        boolean excluded = valueClass.isAnnotationPresent(XssDisable.class);
        if (!excluded) {
            excluded = (valueClass.getComponentType() != null && valueClass.getComponentType().isPrimitive() && valueClass != char[].class);

            if (!excluded) {
                excluded = EXCLUDED_CLASSES.stream().anyMatch(type -> type.isInstance(value));
            }
        }
        return excluded;
    }

    private boolean containsInList(Object value, List<Object> manipulatedObjects) {
        for (Object obj : manipulatedObjects) {
            if (obj == value) {
                return true;
            }
        }
        return false;
    }

    private static class ResponseTuple {
        private Object value;
        private boolean modified;

        public ResponseTuple(final Object value, final boolean modified) {
            this.value = value;
            this.modified = modified;
        }
    }

    private enum DataFlowType {
        INCOMING,
        OUTGOING
    }
}
