// IGNORE_BACKEND: JS_IR, JS, NATIVE, WASM
// IGNORE_BACKEND: JS_IR_ES6
// WITH_REFLECT
package test

import kotlin.test.assertEquals

open class A<T> {
    fun foo(t: T) {}
}

open class B<U> : A<U>()

class C : B<String>()

fun box(): String {
    assertEquals("fun test.A<T>.foo(T): kotlin.Unit", A<Double>::foo.toString())
    assertEquals("fun test.B<U>.foo(U): kotlin.Unit", B<Float>::foo.toString())
    assertEquals("fun test.C.foo(kotlin.String): kotlin.Unit", C::foo.toString())

    konst afoo = A::class.members.single { it.name == "foo" }
    assertEquals("fun test.A<T>.foo(T): kotlin.Unit", afoo.toString())
    konst bfoo = B::class.members.single { it.name == "foo" }
    assertEquals("fun test.B<U>.foo(U): kotlin.Unit", bfoo.toString())
    konst cfoo = C::class.members.single { it.name == "foo" }
    assertEquals("fun test.C.foo(kotlin.String): kotlin.Unit", cfoo.toString())

    return "OK"
}
