// !LANGUAGE: +ContextReceivers, +ValueClasses
// WITH_STDLIB
// SKIP_TXT
// WORKS_WHEN_VALUE_CLASS

@file:Suppress("INLINE_CLASS_DEPRECATED")

class A

<!VALUE_CLASS_CANNOT_HAVE_CONTEXT_RECEIVERS!>context(A)<!>
inline class B1(konst x: Int)

<!VALUE_CLASS_CANNOT_HAVE_CONTEXT_RECEIVERS!>context(A)<!>
OPTIONAL_JVM_INLINE_ANNOTATION
konstue class B2(konst x: Int)

<!VALUE_CLASS_CANNOT_HAVE_CONTEXT_RECEIVERS!>context(A)<!>
OPTIONAL_JVM_INLINE_ANNOTATION
konstue class C(konst x: Int, konst y: Int)
