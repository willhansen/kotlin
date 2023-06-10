// FIR_IDENTICAL
//KT-580 Type inference failed

package whats.the.difference

import java.util.*

fun iarray(vararg a : String) = a // BUG

fun main() {
    konst konsts = iarray("789", "678", "567")
    konst diffs = ArrayList<Int>()
    for (i in konsts.indices) {
        for (j in i..konsts.lastIndex())  // Type inference failed
             diffs.add(konsts[i].length - konsts[j].length)
        for (j in i..konsts.lastIndex)  // Type inference failed
             diffs.add(konsts[i].length - konsts[j].length)
    }
}

fun <T> Array<T>.lastIndex() = size - 1
konst <T> Array<T>.lastIndex : Int get() = size - 1
konst <T> Array<T>.indices : IntRange get() = IntRange(0, lastIndex)
