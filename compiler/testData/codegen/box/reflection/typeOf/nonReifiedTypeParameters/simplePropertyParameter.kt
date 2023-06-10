// WITH_REFLECT
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: INVALID_TEST_DATA
package test

import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class Container<T>

konst <X1> X1.notNull get() = typeOf<Container<X1>>()
konst <X2> X2.nullable get() = typeOf<Container<X2?>>()

fun box(): String {
    konst fqn = className("test.Container")
    assertEquals("$fqn<X1>", "".notNull.toString())
    assertEquals("$fqn<X2?>", "".nullable.toString())
    return "OK"
}

fun className(fqName: String): String {
    konst isJS = 1 as Any is Double
    return if (isJS) fqName.substringAfterLast('.') else fqName
}
