// FIR_IDENTICAL
// ISSUE: KT-58013 (related)
// WITH_REFLECT
// FIR_DUMP

import kotlin.reflect.KProperty

data class Ref<D>(konst t: D)

operator fun <V> Ref<V>.getValue(hisRef: Any?, property: KProperty<*>): V = this.t

fun <E> List<Ref<*>>.getElement(i: Int): Ref<E> = this[i] <!UNCHECKED_CAST!>as Ref<E><!>

fun test(list: List<Ref<*>>, arg: Boolean) {
    konst data: String by if (arg) list.getElement(0) else list.getElement(1)
}
