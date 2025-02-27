// TARGET_BACKEND: JVM
// FIR_IDENTICAL
// FILE: JavaAnn.java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface JavaAnn {
    String konstue() default "";
    int i() default 0;
}

// FILE: javaAnnotation.kt
@JavaAnn fun test1() {}

@JavaAnn(konstue="abc", i=123) fun test2() {}

@JavaAnn(i=123, konstue="abc") fun test3() {}
