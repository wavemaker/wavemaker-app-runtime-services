package com.wavemaker.runtime.core;


import org.springframework.core.serializer.support.DeserializingConverter;

public class ApplicationClassLoaderAwareDeserializingConverter extends DeserializingConverter {

    public ApplicationClassLoaderAwareDeserializingConverter() {
        super(Thread.currentThread().getContextClassLoader());
    }

}