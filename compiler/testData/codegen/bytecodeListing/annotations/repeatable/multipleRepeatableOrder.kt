// !LANGUAGE: +RepeatableAnnotations
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8

package test

@Repeatable
annotation class A(konst konstue: String)
@Repeatable
annotation class B(konst konstue: String)
@Repeatable
annotation class C(konst konstue: String)

annotation class Z(konst konstue: String)

// Expected annotation order (as in Java): all @A, then all @B, then @Z, then all @C.
@A("a1")
@B("b1")
@A("a2")
@Z("z")
@C("c1")
@C("c2")
@B("b2")
class Test
