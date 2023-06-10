// FIR_IDENTICAL
// !LANGUAGE: +RepeatableAnnotations +RepeatableAnnotationContainerConstraints
// FULL_JDK

import java.lang.annotation.Repeatable as R

<!REPEATABLE_CONTAINER_MUST_HAVE_VALUE_ARRAY_ERROR!>@R(C1::class)<!>
annotation class A1
annotation class C1

<!REPEATABLE_CONTAINER_MUST_HAVE_VALUE_ARRAY_ERROR!>@R(C2::class)<!>
annotation class A2
annotation class C2(konst konstue: A2)

<!REPEATABLE_CONTAINER_MUST_HAVE_VALUE_ARRAY_ERROR!>@R(C3::class)<!>
annotation class A3
annotation class C3(konst konstue: Array<String>)

<!REPEATABLE_CONTAINER_MUST_HAVE_VALUE_ARRAY_ERROR!>@R(C4::class)<!>
annotation class A4
annotation class C4(konst notValue: Array<A4>)

<!REPEATABLE_CONTAINER_HAS_NON_DEFAULT_PARAMETER_ERROR!>@R(C5::class)<!>
annotation class A5
annotation class C5(konst konstue: Array<A5>, konst irrelevant: String)

<!REPEATABLE_CONTAINER_HAS_NON_DEFAULT_PARAMETER_ERROR!>@R(C6::class)<!>
annotation class A6
annotation class C6(konst irrelevant: Double, konst konstue: Array<A6> = [])

@R(A7::class)
annotation class A7(konst konstue: Array<A7>)



@R(D1::class)
annotation class B1
annotation class D1(konst konstue: Array<B1>)

@R(D2::class)
annotation class B2
annotation class D2(konst konstue: Array<B2> = [])

@R(D3::class)
annotation class B3
annotation class D3(konst konstue: Array<B3>, konst other1: String = "", konst other2: Int = 42)

@R(D4::class)
annotation class B4
annotation class D4(konst konstue1: Array<B4> = [], konst konstue: Array<B4>)
