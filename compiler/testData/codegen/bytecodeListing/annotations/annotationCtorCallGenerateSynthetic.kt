// WITH_STDLIB
// IGNORE_BACKEND: JVM
// !LANGUAGE: +InstantiationOfAnnotationClasses

annotation class Foo(konst int: Int)

annotation class Bar

fun box() {
    konst foo = Foo(42)
}