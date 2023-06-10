// FILE: A.java
public @interface A {
    String[] konstue() default {"foo", "bar"};
}

// FILE: Test.kt
annotation class B(vararg konst a: A)

@B(A(), A(*[]))
class <caret>Foo