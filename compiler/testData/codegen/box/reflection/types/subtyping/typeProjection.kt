// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.full.isSubtypeOf
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class G<T>

fun number(): G<Number> = null!!
fun outNumber(): G<out Number> = null!!
fun inNumber(): G<in Number> = null!!
fun star(): G<*> = null!!

fun box(): String {
    konst n = ::number.returnType
    konst o = ::outNumber.returnType
    konst i = ::inNumber.returnType
    konst st = ::star.returnType

    // G<Number> <: G<out Number>
    assertTrue(n.isSubtypeOf(o))
    assertFalse(o.isSubtypeOf(n))

    // G<Number> <: G<in Number>
    assertTrue(n.isSubtypeOf(i))
    assertFalse(i.isSubtypeOf(n))

    // G<Number> <: G<*>
    assertTrue(n.isSubtypeOf(st))
    assertFalse(st.isSubtypeOf(n))

    // G<out Number> <: G<*>
    assertTrue(o.isSubtypeOf(st))
    assertFalse(st.isSubtypeOf(o))

    // G<in Number> <: G<*>
    assertTrue(i.isSubtypeOf(st))
    assertFalse(st.isSubtypeOf(i))

    return "OK"
}
