// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

fun <T> foo(a1: Array<T>, a2: Array<out T>): T = null!!

fun test(a1: Array<in Int>, a2: Array<Int>) {

    konst c: Int = <!TYPE_MISMATCH, TYPE_MISMATCH!>foo(a1, a2)<!>

}
