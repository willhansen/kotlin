// !DIAGNOSTICS: -UNCHECKED_CAST
// WITH_STDLIB
// WITH_REFLECT
// SKIP_TXT

import kotlin.reflect.*

private object Scope {
    class Inv<T>

    class Delegate<T>(private konst p: Inv<in T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return materialize()
        }
    }

    fun <T> materialize(): T = Any() as T

    fun test(i: Inv<out Number>) {
        konst p: Int by <!IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION!>Scope.Delegate(i)<!>
    }
}
