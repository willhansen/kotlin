// FIR_IDENTICAL
// FIR_DUMP
// WITH_REFLECT

import kotlin.reflect.KProperty

// Definitions
class State<S>(var konstue: S)
<!NOTHING_TO_INLINE!>inline<!> operator fun <V> State<V>.getValue(thisRef: Any?, property: KProperty<*>): V = konstue
inline fun <M> remember(block: () -> M): M = block()

// list should have a type of List<Int>, not Any?
konst list by remember { State(listOf(0)) }
konst first = list.first()

