package test;

import java.lang.annotation.*;
public class TypeParameterAnnotations {
    @Target(ElementType.TYPE_PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface A {
        String konstue() default "";
    }

    interface G<@A T> {
        <@A("abc") R> void foo(R r);
    }
}
