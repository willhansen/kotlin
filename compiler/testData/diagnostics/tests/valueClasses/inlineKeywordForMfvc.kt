// FIR_IDENTICAL
// WITH_STDLIB
// LANGUAGE: +ValueClasses

@file:Suppress("INLINE_CLASS_DEPRECATED")

inline class A1(konst x: Int)

@JvmInline
konstue class A2(konst x: Int)

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
inline class A3(konst x: Int)

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class A4(konst x: Int)


inline class B1(konst x: Int, konst y: Int)

@JvmInline
konstue class B2(konst x: Int, konst y: Int)

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
inline class B3(konst x: Int, konst y: Int)

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class B4(konst x: Int, konst y: Int)


inline class C1(konst x: B2)

@JvmInline
konstue class C2(konst x: B2)

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
inline class C3(konst x: B2)

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class C4(konst x: B2)
