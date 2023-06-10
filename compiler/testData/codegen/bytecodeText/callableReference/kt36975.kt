// FILE: test.kt
class A(konst konstue: String)

fun box(): String {
    konst ref = A::konstue
    return ref(A("OK"))
}

// Check that non-bound callable references are generated as singletons
// 1 GETSTATIC TestKt\$box\$ref\$1.INSTANCE
