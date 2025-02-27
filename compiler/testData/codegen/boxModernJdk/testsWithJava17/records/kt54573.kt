// TARGET_BACKEND: JVM_IR
// ISSUE: KT-54573
// WITH_STDLIB

@JvmRecord
data class A constructor(konst x: Int, konst s: String) {
    constructor(s: String) : this(s.length, s)
}

fun box(): String = A("OK").s
