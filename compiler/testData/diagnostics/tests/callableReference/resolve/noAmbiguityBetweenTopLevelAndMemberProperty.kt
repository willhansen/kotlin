// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun <R> property(property: KProperty0<R>): Int = 1
fun <T, R> property(property: KProperty1<T, R>): String = ""

konst subject = ""

class O {
    konst subject = ""
}

konst someProperty0 = property(::subject)
konst someProperty1 = property(O::subject)