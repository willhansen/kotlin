// FIR_IDENTICAL
// !DIAGNOSTICS:-UNUSED_VARIABLE

import kotlin.reflect.*

class A(var g: A) {
    konst f: Int = 0

    fun test() {
        konst fRef: KProperty1<A, Int> = A::f
        konst gRef: KMutableProperty1<A, A> = A::g
    }
}
