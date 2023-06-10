// FULL_JDK
// WITH_STDLIB
// !LANGUAGE: -ProhibitEnumDeclaringClass

import java.util.*

enum class SomeEnum { A }

fun bar() {
    SomeEnum.A.<!ENUM_DECLARING_CLASS_DEPRECATED_WARNING!>declaringClass<!>
}

fun <E : Enum<E>> foo(konstues: Array<E>) {
    EnumSet.noneOf(konstues.first().<!ENUM_DECLARING_CLASS_DEPRECATED_WARNING!>declaringClass<!>)
    EnumSet.noneOf(konstues.first().<!UNRESOLVED_REFERENCE!>getDeclaringClass<!>())
}
