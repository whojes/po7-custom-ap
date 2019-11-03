package com.tmax.proobject.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface CkService {
    String serviceName();
    HttpMethod method() default HttpMethod.CUSTOM;
    String customMethod() default "";
}
