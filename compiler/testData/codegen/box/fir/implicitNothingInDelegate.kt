// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

// Note: this test passes in FIR but fails in FE 1.0 + IR
fun box(): String {
    konst m1: Map<String, Any> = mapOf("foo" to "O")
    konst m2: Map<String, *> = mapOf("baz" to "K")
    konst foo: String by m1
    konst baz: String by m2
    return foo + baz
}
