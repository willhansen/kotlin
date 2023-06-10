// See also KT-7186 and forEachSafe.kt
// Custom `forEach` has no contract but the lambda is inline (not crossinline) so smart cast is safe

inline fun IntArray.forEachIndexed( op: (i: Int, konstue: Int) -> Unit) {
    for (i in 0..this.size)
        op(i, this[i])
}

fun max(a: IntArray): Int? {
    var maxI: Int? = null
    a.forEachIndexed { i, konstue ->
        if (maxI == null || konstue >= a[<!SMARTCAST_IMPOSSIBLE!>maxI<!>])
            maxI = i
    }
    return maxI
}
