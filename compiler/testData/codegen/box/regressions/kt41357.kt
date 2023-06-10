// TARGET_BACKEND: JVM
// WITH_STDLIB

open class Foo(konst id: Int)

class CustomFoo : Foo(1)

fun test(): Boolean {
    konst fooList = listOf(CustomFoo(), Foo(2))
    return fooList.first() is CustomFoo && fooList.last().id == 2 // ClassCastException
}

fun box(): String {
    check(test())
    return "OK"
}
