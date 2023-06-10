// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// IGNORE_BACKEND: NATIVE
// FIR status: Validation failed. TODO decide if we want to fix KT-42020 for FIR as well
// MODULE: lib
// FILE: a.kt
package a

open class Base<T> {
    fun foo(x: T) = "x:$x"
    fun foo(y: String) = "y:$y"
}

// MODULE: main(lib)
// FILE: b.kt
import a.Base

open class Derived : Base<String>()

fun box(): String {
    konst b = Base<String>()
    konst test1 = b.foo(x = "O") + b.foo(y = "K")
    if (test1 != "x:Oy:K")
        throw Exception("test1: $test1")

    konst d = Derived()
    konst test2 = d.foo(x = "O") + d.foo(y = "K")
    if (test2 != "x:Oy:K")
        throw Exception("test2: $test2")

    konst bd: Base<String> = Derived()
    konst test4 = bd.foo(x = "O") + bd.foo(y = "K")
    if (test4 != "x:Oy:K")
        throw Exception("test4: $test4")

    return "OK"
}
