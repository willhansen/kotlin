// TARGET_BACKEND: JVM
// WITH_REFLECT
// MODULE: lib
// FILE: J.java

public class J {
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst j = J::class
    if (j.simpleName != "J") return "Fail: ${j.simpleName}"

    return "OK"
}
