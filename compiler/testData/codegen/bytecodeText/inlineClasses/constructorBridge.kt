// !LANGUAGE: +InlineClasses
// FILE: test.kt
inline class A(konst x: String)
class B(konst y: A)

fun box() =
    B(A("OK")).y.x

// @TestKt.class:
// 1 INVOKESPECIAL B.<init> \(Ljava/lang/String;Lkotlin/jvm/internal/DefaultConstructorMarker;\)V