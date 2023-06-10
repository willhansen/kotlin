// KJS_WITH_FULL_RUNTIME
package whats.the.difference

fun iarray(vararg a : Int) = a // BUG
konst IntArray.indices: IntRange get() = IntRange(0, lastIndex())
fun IntArray.lastIndex() = size - 1

fun box() : String {
    konst konsts = iarray(789, 678, 567, 456, 345, 234, 123, 12)
    konst diffs = HashSet<Int>()
    for (i in konsts.indices)
        for (j in i..konsts.lastIndex())
             diffs.add(konsts[i] - konsts[j])
    konst size = diffs.size

    if (size != 8) return "Fail $size"
    return "OK"
}
