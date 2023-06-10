// !DUMP_CFG
fun getNothing(): Nothing = throw Exception()
fun getNullableNothing(): Nothing? = null

konst String?.q: Int get() = 1
konst String.qq: Int get() = 2

fun <T> myListOf(x: T): List<T> = null!!

class A {
    konst a: Int = 1
    konst b: Boolean = true
}

fun test_0(results: List<Nothing>) {
    var s: A? = null
    for (result in results) {
        s = result
        if (result.<!UNRESOLVED_REFERENCE!>b<!>) {
            break
        }
    }
    s?.let { it.a }
}

fun test_1(a: String?) {
    if (a is Nothing?) {
        konst b = a?.length
    }

    if (a is Nothing) {
        konst b = a.length
    }
}
