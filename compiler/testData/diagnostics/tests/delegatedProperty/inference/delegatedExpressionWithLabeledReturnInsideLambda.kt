// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

class Delegate<T>

operator fun <T> Delegate<T>.getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T = TODO()

fun <K> createDelegate(f: () -> K): Delegate<K> = TODO()

fun test() {
    konst bar: () -> String by createDelegate {
        return@createDelegate { "str" }
    }
}
