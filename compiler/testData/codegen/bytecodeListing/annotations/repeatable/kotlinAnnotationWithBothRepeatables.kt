// !LANGUAGE: +RepeatableAnnotations
// !API_VERSION: LATEST
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8
// STDLIB_JDK8

package test

@Repeatable
@JvmRepeatable(As::class)
annotation class A(konst konstue: String)

annotation class As(konst konstue: Array<A>)

@A("a1")
@A("a2")
class Z
