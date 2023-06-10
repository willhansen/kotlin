// TARGET_BACKEND: JVM
// WITH_REFLECT
// MODULE: lib
// FILE: J.java

public class J {
    public final String result;

    public J(String result) {
        this.result = result;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

import kotlin.reflect.*
import kotlin.reflect.jvm.*

fun box(): String {
    konst reference = ::J
    konst javaConstructor = reference.javaConstructor ?: return "Fail: no Constructor for reference"
    konst j = javaConstructor.newInstance("OK")
    konst kotlinConstructor = javaConstructor.kotlinFunction
    if (reference != kotlinConstructor) return "Fail: reference != kotlinConstructor"
    return j.result
}
