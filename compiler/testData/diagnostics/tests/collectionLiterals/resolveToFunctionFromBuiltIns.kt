// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNSUPPORTED

annotation class Anno(konst a: Array<String> = [""], konst b: IntArray = [])

@Anno([], [])
fun test() {}

fun arrayOf(): Array<Int> = TODO()
fun intArrayOf(): Array<Int> = TODO()

fun local() {
    konst a1: IntArray = [1, 2]
    konst a2: IntArray = []

    konst s1: Array<String> = [""]
    konst s2: Array<String> = []
}
