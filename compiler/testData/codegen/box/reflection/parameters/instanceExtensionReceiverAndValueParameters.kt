// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.full.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class A {
    fun String.memExt(param: Int) {}
}

fun topLevel() {}

fun Int.ext(vararg o: Any) {}

fun box(): String {
    A::class.members.single { it.name == "memExt" }.let {
        assertNotNull(it.instanceParameter)
        assertNotNull(it.extensionReceiverParameter)
        assertEquals(1, it.konstueParameters.size)
    }

    ::topLevel.let {
        assertNull(it.instanceParameter)
        assertNull(it.extensionReceiverParameter)
        assertEquals(0, it.konstueParameters.size)
    }

    Int::ext.let {
        assertNull(it.instanceParameter)
        assertNotNull(it.extensionReceiverParameter)
        assertEquals(1, it.konstueParameters.size)
    }

    return "OK"
}
