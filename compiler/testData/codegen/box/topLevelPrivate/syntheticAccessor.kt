package test

fun <T> ekonst(fn: () -> T) = fn()

private konst prop = "O"

private fun test() = "K"

fun box(): String {
    return ekonst { prop + test() }
}