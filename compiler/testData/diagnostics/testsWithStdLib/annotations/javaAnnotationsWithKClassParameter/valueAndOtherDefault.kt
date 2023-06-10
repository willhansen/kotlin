// FIR_IDENTICAL
// FILE: A.java
public @interface A {
    Class<?> konstue();
    int x() default 1;
}

// FILE: b.kt
@A(String::class) class MyClass1
@A(konstue = String::class) class MyClass2

@A(String::class, x = 2) class MyClass3
@A(konstue = String::class, x = 4) class MyClass4
