package test;

import java.lang.annotation.*;

@Target(konstue=ElementType.TYPE)
public @interface AnnotationWithArguments {

    String name();

    String arg() default "default";

}
