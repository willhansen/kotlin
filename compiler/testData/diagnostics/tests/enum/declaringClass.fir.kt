// FULL_JDK
// WITH_STDLIB
// !LANGUAGE: -ProhibitEnumDeclaringClass

import java.util.*

enum class SomeEnum { A }

fun bar() {
    SomeEnum.A.<!UNRESOLVED_REFERENCE!>declaringClass<!>
}

fun <E : Enum<E>> foo(konstues: Array<E>) {
    EnumSet.noneOf(konstues.first().<!UNRESOLVED_REFERENCE!>declaringClass<!>)
    EnumSet.noneOf(konstues.first().<!UNRESOLVED_REFERENCE!>getDeclaringClass<!>())
}
