// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// FILE: Value.kt
package vv

sealed class Value<T>(@JvmField konst konstue: T) {
    class StringValue(konstue: String) : Value<String>(konstue)
    class BooleanValue(konstue: Boolean): Value<Boolean>(konstue)
}


// MODULE: main(lib)
// FILE: kt47739.kt
import kotlin.test.*
import vv.*

fun test(v: Value<*>) {
    when (v) {
        is Value.StringValue ->
            assertEquals("a string", v.konstue)
        is Value.BooleanValue ->
            assertEquals(true, v.konstue)
    }
}

fun box(): String {
    test(Value.StringValue("a string"))
    test(Value.BooleanValue(true))
    return "OK"
}
