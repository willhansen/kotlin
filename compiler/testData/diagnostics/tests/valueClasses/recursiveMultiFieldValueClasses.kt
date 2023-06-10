// FIR_IDENTICAL
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses


@JvmInline
konstue class A1(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A1<!>)

@JvmInline
konstue class B1(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>B1<!>, konst y: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>B1<!>)


@JvmInline
konstue class A2(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>B2<!>)

@JvmInline
konstue class B2(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A2<!>, konst y: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A2<!>)


@JvmInline
konstue class A3(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>B3<!>)

@JvmInline
konstue class B3(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A3<!>)


@JvmInline
konstue class A4(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>B4<!>, konst y: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>B4<!>)

@JvmInline
konstue class B4(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A4<!>, konst y: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A4<!>)

@JvmInline
konstue class C4(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>D4?<!>, konst y: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>D4?<!>)

@JvmInline
konstue class D4(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>D4?<!>, konst y: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>C4?<!>)

@JvmInline
konstue class E4(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>E4?<!>, konst y: Int)

@JvmInline
konstue class F4(konst x: Int, konst y: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>F4?<!>)



@JvmInline
konstue class A5<T : A5<T>>(konst x: T)

@JvmInline
konstue class B5<T : B5<T>>(konst x: T, konst y: T)


@JvmInline
konstue class A6<T : B6<<!UPPER_BOUND_VIOLATED!>T<!>>>(konst x: T, konst y: T)

@JvmInline
konstue class B6<T : A6<<!UPPER_BOUND_VIOLATED!>T<!>>>(konst x: T)


@JvmInline
konstue class A7<T : B7<<!UPPER_BOUND_VIOLATED!>T<!>>>(konst x: T, konst y: T)

@JvmInline
konstue class B7<T : A7<<!UPPER_BOUND_VIOLATED!>T<!>>>(konst x: T, konst y: T)


@JvmInline
konstue class A8<T : B8<<!UPPER_BOUND_VIOLATED!>T<!>>>(konst x: T?, konst y: T?)

@JvmInline
konstue class B8<T : A8<<!UPPER_BOUND_VIOLATED!>T<!>>>(konst x: T?, konst y: T?)

interface I1
interface I2

@JvmInline
konstue class A<T, G : C?>(
    konst t1: List<T>,
    konst t2: UInt,
    konst t3: List<G?>,
    konst t4: UInt,
    konst t5: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>C<!>,
    konst t6: Int,
    konst t7: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>B<!>,
    konst t8: String,
    konst t9: T,
    konst t10: Char,
    konst t11: T?,
) where T : I1, T : B?, T : I2

@JvmInline
konstue class B(konst x: UInt, konst a: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A<B, Nothing><!>) : I1, I2

@JvmInline
konstue class C(konst x: UInt, konst a: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>A<B, Nothing><!>)
