// !DIAGNOSTICS: -UNUSED_PARAMETER

interface A
interface B {
    fun test() {}
}

fun <K> select(a: K, b: K): K = a

fun test(a: A?, b: B?) {
    b as A?
    a as B?
    konst c = select(a, b)
    if (c != null) {
        <!DEBUG_INFO_SMARTCAST!>c<!>.test()
    }
}