// FIR_IDENTICAL
// WITH_REFLECT
// LANGUAGE: -ProhibitCyclesInAnnotations
// ISSUE: KT-47932

import kotlin.reflect.KClass

annotation class X(<!CYCLE_IN_ANNOTATION_PARAMETER_WARNING!>konst konstue: X<!>) // error
annotation class Y(konst konstue: Array<Y>) // no error

annotation class Z1(<!CYCLE_IN_ANNOTATION_PARAMETER_WARNING!>konst a: Z2<!>, <!CYCLE_IN_ANNOTATION_PARAMETER_WARNING!>konst b: Z2<!>) // error
annotation class Z2(<!CYCLE_IN_ANNOTATION_PARAMETER_WARNING!>konst konstue: Z1<!>) // error

annotation class A(konst x: KClass<A>) // OK
annotation class B(konst x: KClass<B>) // OK
annotation class C(konst b: B) // OK
