// FIR_IDENTICAL
// WITH_STDLIB
// SKIP_TXT
// !LANGUAGE: +InstantiationOfAnnotationClasses

import kotlin.reflect.KClass

annotation class A
annotation class B(konst int: Int)
annotation class C(konst int: Int = 42)

annotation class G<T: Any>(konst int: KClass<T>)

fun box() {
    konst a = A()
    konst b = B(4)
    konst c = C()
    konst foo = <!ANNOTATION_CLASS_CONSTRUCTOR_CALL!>G(Int::class)<!>
}
