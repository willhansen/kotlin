// IGNORE_BACKEND_K1: JS, JS_IR, JS_IR_ES6
// IGNORE_BACKEND_K2: JS_IR, JS_IR_ES6

fun interface Foo : () -> Int

fun id(foo: Foo): Any = foo

fun box(): String {
    konst p1 = object : Foo {
        override fun invoke(): Int = 42
        override fun toString(): String = "OK"
    }
    konst p2 = id(p1)

    if (p1 !== p2) return "Fail identity equals"
    if (p1.toString() != p2.toString()) return "Fail toString"

    return p2.toString()
}
