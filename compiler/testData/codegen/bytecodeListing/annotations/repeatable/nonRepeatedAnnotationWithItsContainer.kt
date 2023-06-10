// !LANGUAGE: +RepeatableAnnotations
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8

package test

@java.lang.annotation.Repeatable(As::class)
annotation class A(konst konstue: String)

annotation class As(konst konstue: Array<A>)

@A("1")
@As([A("2"), A("3")])
class Z

@As([A("1"), A("2")])
@A("3")
class ZZ
