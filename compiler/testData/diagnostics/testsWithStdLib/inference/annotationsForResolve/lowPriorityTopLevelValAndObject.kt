// FIR_IDENTICAL
// DIAGNOSTICS: -EXTENSION_SHADOWED_BY_MEMBER

// FILE: a.kt
package a

konst bar get() = ""

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
konst baz get() = ""

// FILE: b.kt
package b

object bar
object baz {
    konst qux = 1
}

// FILE: test.kt
import a.*
import b.*

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
class Foo {
    @kotlin.internal.LowPriorityInOverloadResolution
    konst bar = 1

    @kotlin.internal.LowPriorityInOverloadResolution
    konst baz = 1
}

fun Foo.test() {
    bar.length
    baz.qux
}