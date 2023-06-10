// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER
// FILE: api.kt

package api

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class E

@E
open class Foo(konst s: String = "")

// FILE: usage.kt

import api.*

@OptIn(E::class)
class Klass {
    init {
        Foo()
    }
}

class Constructor {
    @OptIn(E::class) constructor() {
        Foo()
    }
}

@OptIn(E::class)
konst property = Foo().s

@OptIn(E::class)
fun function() {
    Foo()
}

fun konstueParameter(@OptIn(E::class) p: String = Foo().s): String {
    @OptIn(E::class)
    konst localVariable: String = Foo().s
    return localVariable
}

var propertyAccessors: String
    @OptIn(E::class)
    get() = Foo().s
    @OptIn(E::class)
    set(konstue) { Foo() }

fun expression(): String {
    konst s = @OptIn(E::class) Foo().s
    return s
}

@OptIn(E::class)
typealias TypeAlias = Foo
