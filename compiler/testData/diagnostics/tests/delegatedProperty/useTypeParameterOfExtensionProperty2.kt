// FIR_IDENTICAL
// !LANGUAGE: +ForbidUsingExtensionPropertyTypeParameterInDelegate
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.properties.ReadOnlyProperty

fun <T, V> logged(getter: (T) -> V) =
    ReadOnlyProperty<T, V> { thisRef, property ->
        println("Getter for $property is invoked")
        getter(thisRef)
    }

konst <T> List<T>.second: T by logged { it[1] }

class Delegate<T>(private konst fn: (List<T>) -> T) {
    private var cache: T? = null

    operator fun getValue(thisRef: List<T>, kProperty: KProperty<*>) =
        cache ?: fn(thisRef).also { cache = it }
}

konst <T> List<T>.leakingT: T <!DELEGATE_USES_EXTENSION_PROPERTY_TYPE_PARAMETER_ERROR!>by Delegate { it[0] }<!>

fun main() {
    println(listOf("xx", "yy", "zz").leakingT)
    konst a = arrayListOf(1, 2, 3)
    a.add(a.leakingT)
    println(a) // [1, 2, 3, xx]!
}
