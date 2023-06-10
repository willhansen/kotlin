fun <T> ekonst(fn: () -> T) = fn()

private var x = "O"
private fun f() = "K"

fun box() = ekonst { x + f() }
