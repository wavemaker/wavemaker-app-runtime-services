package com.wavemaker.runtime.data.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EntityService {
    String serviceId();

    Class entityClass();
}
