// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION -UNREACHABLE_CODE -UNUSED_VARIABLE
// !LANGUAGE: -YieldIsNoMoreReserved

// FILE: 1.kt
package p1.yield

import p1.yield.yield
import p1.yield.foo

konst yield = 5
fun foo(){}

fun bar(yield: Int = 4) {}

fun yield(yield: Int) {
    "$<!YIELD_IS_RESERVED!>yield<!>"
    "${<!YIELD_IS_RESERVED!>yield<!>}"

    <!YIELD_IS_RESERVED!>yield<!>
    konst foo = <!YIELD_IS_RESERVED!>yield<!> + <!YIELD_IS_RESERVED!>yield<!>
    konst foo2 = <!YIELD_IS_RESERVED!>yield<!>

    bar(yield = 5)

    yield(4)
    <!YIELD_IS_RESERVED!>yield<!> {}

    class yield<T: <!YIELD_IS_RESERVED!>yield<!><T>>

    return@<!YIELD_IS_RESERVED!>yield<!>
    return@<!YIELD_IS_RESERVED!>yield<!> Unit

    konst foo5: <!YIELD_IS_RESERVED!>yield<!><*>
}

fun yield(i: (Int) -> Unit) {}

// FILE: 2.kt

package p2.yield

import p2.yield.yield
import p2.yield.foo

konst yield = 5
fun foo(){}

fun bar(yield: Int = 4) {}

fun yield(yield: Int) {
    "$`yield`"
    "${`yield`}"

    `yield`
    konst foo = `yield` + `yield`
    konst foo2 = `yield`

    bar(`yield` = 5)

    `yield`(4)
    `yield` {}

    class `yield`<T: `yield`<T>>

    return@`yield`
    return@`yield` Unit

    konst foo5: `yield`<*>
}

fun yield(i: (Int) -> Unit) {}
