package com.rdn.prompt.auth;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @Description 设置资源访问权限的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Permission {

    @AliasFor("value")
    RoleEnum[] role() default {};

    @AliasFor("role")
    RoleEnum[] value() default {};
}
