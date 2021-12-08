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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
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

    public WMRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
        super(converters, contentNegotiationManager);
    }

    public WMRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters, List<Object> requestResponseBodyAdvice) {
        super(converters, requestResponseBodyAdvice);
    }

    public WMRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager, List<Object> requestResponseBodyAdvice) {
        super(converters, manager, requestResponseBodyAdvice);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        Method method = returnType.getMethod();
        boolean xssDisable;

        xssDisable = Arrays.stream(Objects.requireNonNull(method).getDeclaringClass().getDeclaredAnnotations())
                .anyMatch(an -> an instanceof XssDisable) || Arrays.stream(method.getDeclaredAnnotations())
                .anyMatch(an -> an instanceof XssDisable);

        if (!xssDisable) {
            ResponseTuple encodeResult = encode(returnValue, new ArrayList());
            if(encodeResult.modified) {
                returnValue = encodeResult.value;
            }
        }
        super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    private ResponseTuple encode(Object value, List<Object> manipulatedObjects) {
        Object encoded = value;
        boolean modified = false;

        if (value != null && !(value instanceof Number) && !(containsInList(value, manipulatedObjects) || isExcludedClass(value))) {
            final Class<?> valueClass = value.getClass();

            if (valueClass == char[].class) {
                encoded = encode(((char[]) value));
                modified = true;
            } else if (valueClass.isArray()) {
                modified = encodeArray((Object[]) value, manipulatedObjects);
            } else if (value instanceof String) {
                encoded = encode((String) value);
                modified = true;
            } else {
                manipulatedObjects.add(value);
                final ResponseTuple response = encodeCustomClass(value, manipulatedObjects);
                if (response.modified) {
                    encoded = response.value;
                    modified = true;
                }
            }
        }

        return new ResponseTuple(encoded, modified);
    }

    private boolean encodeArray(
            final Object[] valueArray, final List<Object> manipulatedObjects) {
        boolean modified = false;
        for (int i = 0; i < valueArray.length; i++) {
            final Object obj = valueArray[i];
            final ResponseTuple result = encode(obj, manipulatedObjects);
            if (result.modified) {
                valueArray[i] = result.value;
                modified = true;
            }
        }
        return modified;
    }


    private ResponseTuple encodeCustomClass(Object object, List<Object> manipulatedObjects) {
        AtomicBoolean modified = new AtomicBoolean(false);
        if(object instanceof Page) {
            ((Page) object).getContent().forEach(obj -> {
                obj = encode(obj, manipulatedObjects).value;
                modified.set(true);
            });
        }
        for (final Field field : object.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(XssDisable.class) && !Modifier.isFinal(field.getModifiers())) {
                ReflectionUtils.makeAccessible(field);
                try {
                    final ResponseTuple response = encode(field.get(object), manipulatedObjects);
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

    private char[] encode(char[] value) {
        return encode(new String(value)).toCharArray();
    }

    private String encode(String value) {
        return XSSSecurityHandler.getInstance().sanitizeRequestData(value);
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

    private static class ResponseTuple {

        private Object value;
        private boolean modified;

        public ResponseTuple(final Object value, final boolean modified) {
            this.value = value;
            this.modified = modified;
        }
    }

    private boolean containsInList(Object value, List<Object> manipulatedObjects) {
        for(Object obj : manipulatedObjects) {
            if(obj == value) {
                return true;
            }
        }
        return false;
    }
}
