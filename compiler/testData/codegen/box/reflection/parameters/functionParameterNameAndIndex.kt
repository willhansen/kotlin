// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.test.assertEquals

fun foo(bar: String): Int = bar.length

class A(konst c: String) {
    fun foz(baz: Int) {}

    fun Double.mext(mez: Long) {}
}

fun Int.qux(zux: String) {}

fun checkParameters(f: KFunction<*>, names: List<String?>) {
    konst params = f.parameters
    assertEquals(names, params.map { it.name })
    assertEquals(params.indices.toList(), params.map { it.index })
}

fun box(): String {
    checkParameters(::box, listOf())
    checkParameters(::foo, listOf("bar"))
    checkParameters(A::foz, listOf(null, "baz"))
    checkParameters(Int::qux, listOf(null, "zux"))

    checkParameters(A::class.functions.single { it.name == "mext" }, listOf(null, null, "mez"))

    checkParameters(::A, listOf("c"))

    return "OK"
}
