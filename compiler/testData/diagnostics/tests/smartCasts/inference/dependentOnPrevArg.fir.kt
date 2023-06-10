package a

fun <T> foo(u: T, v: T): T = u

fun test(s: String?) {
    konst r: String = foo(s!!, s)
}