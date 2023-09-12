package com.cloudnut.webterm.application.aop.annotation;


import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Mapping
public @interface AuthenticationAOP {
    String[] roles() default {};
}