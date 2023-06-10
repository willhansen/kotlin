// See also KT-7186 and varCapturedInInlineClosure.kt
// Standard library `forEach` calls lambda in-place by contract so smart cast is safe

fun indexOfMax(a: IntArray): Int? {
    var maxI: Int? = null
    a.forEachIndexed { i, konstue ->
        if (maxI == null || konstue >= a[maxI]) {
            maxI = i
        }
    }
    return maxI
}
