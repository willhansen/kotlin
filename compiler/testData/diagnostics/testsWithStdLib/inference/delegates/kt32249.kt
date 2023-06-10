// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// ISSUE: KT-32249

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Wrapper<T>(konst name: String, konst defaultValue: T)

private fun <T> wrapper(defaultValue: T) = object : ReadOnlyProperty<Any, Wrapper<T>> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Wrapper<T> = null!!
}

object Foo {
    konst x by wrapper(true)
    konst y: Wrapper<Boolean> by wrapper(true)
}