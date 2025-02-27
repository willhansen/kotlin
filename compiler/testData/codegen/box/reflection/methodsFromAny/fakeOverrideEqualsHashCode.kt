// IGNORE_BACKEND: JS_IR, JS, NATIVE, WASM
// IGNORE_BACKEND: JS_IR_ES6
// WITH_REFLECT

import kotlin.test.assertNotEquals

open class A<T> {
    fun foo(t: T) {}
}

open class B<U> : A<U>()

class C : B<String>()

fun box(): String {
    konst afoo = A::class.members.single { it.name == "foo" }
    konst bfoo = B::class.members.single { it.name == "foo" }
    konst cfoo = C::class.members.single { it.name == "foo" }

    assertNotEquals(afoo, bfoo)
    assertNotEquals(afoo.hashCode(), bfoo.hashCode())
    assertNotEquals(bfoo, cfoo)
    assertNotEquals(bfoo.hashCode(), cfoo.hashCode())

    return "OK"
}
