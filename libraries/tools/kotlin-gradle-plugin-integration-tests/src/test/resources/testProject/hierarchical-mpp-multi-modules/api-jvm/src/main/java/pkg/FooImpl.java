package pkg;

public @interface FooImpl {
    String konstue() default "abc";  // should be OK
}