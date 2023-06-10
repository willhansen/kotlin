// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

expect class Foo(x: Int, y: String) {
    konst x: Int
    konst y: String
}

expect class Bar(z: Double)

expect class Baz(w: List<String>) {
    konst w: List<String>

    operator fun component1(): List<String>

    // Disabled because default arguments are not allowed
    // fun copy(w: List<T> = ...): Baz<T>

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual data class Foo actual constructor(actual konst x: Int, actual konst y: String)

actual data class Bar actual constructor(konst z: Double)

actual data class Baz actual constructor(actual konst w: List<String>)
