// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.full.*
import kotlin.test.assertTrue
import kotlin.test.assertFalse

open class G<T>
class A : G<String>()

fun gOfString(): G<String> = null!!
fun gOfInt(): G<Int> = null!!

fun box(): String {
    konst gs = ::gOfString.returnType
    konst gi = ::gOfInt.returnType
    konst a = ::A.returnType

    assertTrue(a.isSubtypeOf(gs))
    assertTrue(gs.isSupertypeOf(a))

    assertFalse(a.isSubtypeOf(gi))
    assertFalse(gi.isSupertypeOf(a))

    assertFalse(gs.isSubtypeOf(gi))
    assertFalse(gs.isSupertypeOf(gi))
    assertFalse(gi.isSubtypeOf(gs))
    assertFalse(gi.isSupertypeOf(gs))

    return "OK"
}
