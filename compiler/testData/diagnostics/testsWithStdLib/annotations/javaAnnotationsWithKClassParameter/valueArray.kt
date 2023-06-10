// FIR_IDENTICAL
// FILE: A.java
public @interface A {
    Class<?>[] konstue();
}

// FILE: b.kt
@A(String::class, Int::class) class MyClass1
@A(*arrayOf(String::class, Int::class)) class MyClass2
@A(konstue = [String::class, Int::class]) class MyClass3
