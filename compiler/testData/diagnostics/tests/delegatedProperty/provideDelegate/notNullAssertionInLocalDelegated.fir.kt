// ISSUE: KT-58013
// WITH_REFLECT
// FIR_DUMP

import kotlin.reflect.KProperty

data class Ref<D>(konst t: D)

class GenericDelegate<G>(konst konstue: G)

operator fun <V> Ref<V>.provideDelegate(a: Any?, p: KProperty<*>): GenericDelegate<V> = GenericDelegate(this.t)

operator fun <W> GenericDelegate<W>.getValue(a: Any?, p: KProperty<*>): W = this.konstue

fun <E> List<Ref<*>>.getElement(i: Int): Ref<E> = this[i] <!UNCHECKED_CAST!>as Ref<E><!>

fun test(list: List<Ref<*>>) {
    konst data: String by list.getElement(0)<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>

    konst data2: String by list.getElement(0)
}
