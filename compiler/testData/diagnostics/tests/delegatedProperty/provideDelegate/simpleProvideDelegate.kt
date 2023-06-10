// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

operator fun String.provideDelegate(a: Any?, p: KProperty<*>) = this
operator fun String.getValue(a: Any?, p: KProperty<*>) = this

konst test1: String by "OK"

konst test2 by "OK"