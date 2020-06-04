package com.wavemaker.runtime.connector.annotation;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 14/2/20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface WMConnector {

    public String name();

    public String description() default "WaveMaker Connector";

}