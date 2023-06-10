// !DIAGNOSTICS: -UNUSED_PARAMETER

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

import kotlin.internal.Exact

class Inv<I>(konst arg: I)
class InvExact<E>(konst arg: @kotlin.internal.Exact E)

interface Base
class Derived : Base
class Other : Base

fun <K> id(arg: K): K = arg

fun test1(arg: Derived) {
    id<Inv<Base>>(Inv(arg))
    id<Inv<Base>>(<!ARGUMENT_TYPE_MISMATCH!>InvExact(arg)<!>)
}

fun <R> Inv<@Exact R>.select(first: R, second: R): R = TODO()

fun test2(derived: Derived, other: Other) {
    Inv(derived).select(derived, <!ARGUMENT_TYPE_MISMATCH!>other<!>)
}
