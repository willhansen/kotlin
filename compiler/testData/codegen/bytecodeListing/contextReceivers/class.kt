// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class Outer {
    konst x: Int = 1
}

context(Outer)
class Inner(arg: Any) {
    fun bar() = x
}

fun f(outer: Outer) {
    with(outer) {
        Inner(3)
    }
}
