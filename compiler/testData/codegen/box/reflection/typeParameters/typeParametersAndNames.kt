// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.KClass
import kotlin.test.assertEquals

class F {
    fun <A> foo() {}
    konst <B> B.bar: B get() = this
}

class C<D> {
    fun baz() {}
    fun <E, G> quux() {}
}

fun get(klass: KClass<*>, memberName: String? = null): List<String> =
        (if (memberName != null)
            klass.members.single { it.name == memberName }.typeParameters
        else
            klass.typeParameters)
        .map { it.name }

fun box(): String {
    assertEquals(listOf(), get(F::class))
    assertEquals(listOf("A"), get(F::class, "foo"))
    assertEquals(listOf("B"), get(F::class, "bar"))

    assertEquals(listOf("D"), get(C::class))
    assertEquals(listOf(), get(C::class, "baz"))
    assertEquals(listOf("E", "G"), get(C::class, "quux"))

    assertEquals(listOf("T"), get(Comparable::class))
    assertEquals(listOf(), get(String::class))

    return "OK"
}
