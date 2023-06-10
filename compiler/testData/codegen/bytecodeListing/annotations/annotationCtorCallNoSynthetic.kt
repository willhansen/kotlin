// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

annotation class Foo(konst bar: Bar)

annotation class Bar

@Foo(Bar())
fun box() {
}