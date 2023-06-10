// FIR_IDENTICAL
// !CHECK_TYPE

fun test(a: Array<out String>) {
    konst b = a.toList()

    b checkType { _<List<String>>() }
}