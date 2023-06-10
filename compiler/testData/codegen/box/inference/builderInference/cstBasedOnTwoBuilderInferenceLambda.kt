// WITH_STDLIB
// IGNORE_BACKEND: WASM
// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status: ARGUMENT_TYPE_MISMATCH at contribute arguments
// TARGET_BACKEND: JVM

import kotlin.experimental.ExperimentalTypeInference

class In<in K> {
    fun contribute(x: K) {}
}

class Out<out K> {
    fun get(): K = null as K
}

class Inv<K> {
    fun get(): K = null as K
}

interface A
class B: A
class C: A

@OptIn(ExperimentalTypeInference::class)
fun <K> build1(@BuilderInference builderAction1: In<K>.() -> Unit, @BuilderInference builderAction2: In<K>.() -> Unit): K = 1 as K

@OptIn(ExperimentalTypeInference::class)
fun <K> build2(@BuilderInference builderAction1: In<K>.() -> Unit, @BuilderInference builderAction2: In<K>.() -> Unit): K = B() as K

@OptIn(ExperimentalTypeInference::class)
fun <K> build3(@BuilderInference builderAction1: Out<K>.() -> Unit, @BuilderInference builderAction2: Out<K>.() -> Unit): K = 1 as K

@OptIn(ExperimentalTypeInference::class)
fun <K> build4(@BuilderInference builderAction1: Out<K>.() -> Unit, @BuilderInference builderAction2: Out<K>.() -> Unit): K = B() as K

@OptIn(ExperimentalTypeInference::class)
fun <K> build5(@BuilderInference builderAction1: Inv<K>.() -> Unit, @BuilderInference builderAction2: Inv<K>.() -> Unit): K = 1 as K

@OptIn(ExperimentalTypeInference::class)
fun <K> build6(@BuilderInference builderAction1: Inv<K>.() -> Unit, @BuilderInference builderAction2: Inv<K>.() -> Unit): K = B() as K

@OptIn(ExperimentalStdlibApi::class)
fun box(): String {
    konst x1 = build1({ contribute(1f) }, { contribute(1.0) })
    <!DEBUG_INFO_EXPRESSION_TYPE("{Comparable<*> & Number}")!>x1<!>

    konst y1 = build2({ contribute(B()) }, { contribute(C()) })
    <!DEBUG_INFO_EXPRESSION_TYPE("A")!>y1<!>

    konst x2 = build3({ konst x: Float = get() }, { konst x: Double = get() })
    <!DEBUG_INFO_EXPRESSION_TYPE("{Comparable<*> & Number}")!>x2<!>

    konst y2 = build4({ konst x: B = get() }, { konst x: C = get() })
    <!DEBUG_INFO_EXPRESSION_TYPE("A")!>y2<!>

    konst x3 = build5({ konst x: Float = get() }, { konst x: Double = get() })
    <!DEBUG_INFO_EXPRESSION_TYPE("{Comparable<*> & Number}")!>x3<!>

    konst y3 = build6({ konst x: B = get() }, { konst x: C = get() })
    <!DEBUG_INFO_EXPRESSION_TYPE("A")!>y3<!>

    return "OK"
}
