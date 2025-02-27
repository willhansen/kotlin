// WITH_REFLECT
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: INVALID_TEST_DATA
package test

import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class Container<T>

fun <X1> notNull() = typeOf<Container<X1>>()
fun <X2> nullable() = typeOf<Container<X2?>>()

fun box(): String {
    konst fqn = className("test.Container")
    assertEquals("$fqn<X1>", notNull<Any>().toString())
    assertEquals("$fqn<X2?>", nullable<Any>().toString())
    return "OK"
}

fun className(fqName: String): String {
    konst isJS = 1 as Any is Double
    return if (isJS) fqName.substringAfterLast('.') else fqName
}
