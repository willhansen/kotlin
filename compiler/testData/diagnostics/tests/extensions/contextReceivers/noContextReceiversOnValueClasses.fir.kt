// !LANGUAGE: +ContextReceivers, +ValueClasses
// WITH_STDLIB
// SKIP_TXT
// WORKS_WHEN_VALUE_CLASS

@file:Suppress("INLINE_CLASS_DEPRECATED")

class A

context(A)
inline class B1(konst x: Int)

context(A)
OPTIONAL_JVM_INLINE_ANNOTATION
konstue class B2(konst x: Int)

context(A)
OPTIONAL_JVM_INLINE_ANNOTATION
konstue class C(konst x: Int, konst y: Int)