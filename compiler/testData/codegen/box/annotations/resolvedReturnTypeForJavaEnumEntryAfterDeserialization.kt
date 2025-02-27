// TARGET_BACKEND: JVM

// MODULE: lib
// FILE: Enum.java

public enum Enum {
    Value
}

// FILE: C.java

public class C {
}

// FILE: ArgumentsSource.java

public @interface ArgumentsSource {
    Class<?> konstue();
}

// FILE: MethodSource.java
@ArgumentsSource(C.class)
public @interface MethodSource {
    String[] konstue() default "";
}

// FILE: test.kt
import Enum

annotation class Ann(konst e: Enum)

@Ann(Enum.Value) // Checking Java enchancement after deserialization
@MethodSource("getTestFiles")
fun test(): String {
    return "OK"
}

// MODULE: main(lib)
// FILE: main.kt
@MethodSource("getTestFiles")
fun box(): String {
    return test()
}