package org.jetbrains.annotations;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface NotNull {
    String konstue() default "";

    Class<? extends Exception> exception() default Exception.class;
}
