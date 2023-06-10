// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

operator fun String.provideDelegate(a: Any?, p: KProperty<*>) = this
operator fun String.getValue(a: Any?, p: KProperty<*>) = this

fun test(): String {
    konst result by "OK"
    return result
}