// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER, -EXTENSION_SHADOWED_BY_MEMBER

import kotlin.reflect.*

fun <T> ofType(x: T): T = x

class A {
    konst foo: Int = 0
    fun foo() {}

    fun bar() {}
    konst bar: Int = 0
}

fun A.foo(): String = "A"

konst x0 = A::<!OVERLOAD_RESOLUTION_AMBIGUITY!>foo<!>

konst x1 = ofType<(A) -> Unit>(A::foo)
konst x2 = ofType<KProperty1<A, Int>>(A::foo)
konst x3: KProperty1<A, Int> = A::foo
konst x4: (A) -> String = A::foo

konst y0 = A::<!OVERLOAD_RESOLUTION_AMBIGUITY!>bar<!>
konst y1 = ofType<(A) -> Unit>(A::bar)
konst y2 = ofType<KProperty1<A, Int>>(A::bar)
konst y3: KProperty1<A, Int> = A::bar
