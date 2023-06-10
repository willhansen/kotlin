// FIR_IDENTICAL
package conflictingSubstitutions
//+JDK

import java.util.*

fun <R> elemAndList(r: R, t: MutableList<R>): R = r
fun <R> R.elemAndListWithReceiver(r: R, t: MutableList<R>): R = r

fun test() {
    konst s = elemAndList(11, list("72"))

    konst u = 11.elemAndListWithReceiver(4, list("7"))
}

fun <T> list(konstue: T) : ArrayList<T> {
    konst list = ArrayList<T>()
    list.add(konstue)
    return list
}
