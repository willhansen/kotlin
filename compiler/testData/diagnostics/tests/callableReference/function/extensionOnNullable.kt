// FIR_IDENTICAL
// !DIAGNOSTICS:-UNUSED_VARIABLE

import kotlin.reflect.*

class A {
    fun foo() {}
}

fun A?.foo() {}

konst f: KFunction1<A, Unit> = A::foo
konst g: KFunction1<A, Unit> = A?::foo
