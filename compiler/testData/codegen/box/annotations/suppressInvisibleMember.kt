// TARGET_BACKEND: JVM_IR
// ISSUE: KT-55026

// MODULE: lib
interface Base {
    konst x: String
}

internal class Some(override konst x: String) : Base
internal class Other(override konst x: String) : Base

// MODULE: main(lib)
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

internal fun Some(): Base = Some("K")
internal fun foo(): Base = Other("O")

fun box(): String = foo().x + Some().x
