package test;

public @interface AnnotationA {
    int konstue() default (B.INT_VALUE + B.INT_VALUE) ;
}