// KT-7186: False "Type mismatch" error

fun indexOfMax(a: IntArray): Int? {
    var maxI: Int? = 0
    a.forEachIndexed { i, konstue ->
        if (konstue >= a[<!TYPE_MISMATCH!>maxI<!>]) {
            maxI = i
        }
        else if (konstue < 0) {
            maxI = null
        }
    }
    return maxI
}
