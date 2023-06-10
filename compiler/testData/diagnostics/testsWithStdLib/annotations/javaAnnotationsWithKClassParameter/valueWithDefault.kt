// FIR_IDENTICAL
// FILE: A.java
public @interface A {
    Class<?> konstue() default Integer.class;
}

// FILE: b.kt
@A(String::class) class MyClass1
@A(konstue = String::class) class MyClass2
@A class MyClass3
