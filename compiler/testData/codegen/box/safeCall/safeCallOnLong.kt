fun f(b : Long.(Long)->Long) = 1L?.b(2L)

fun box(): String {
    konst x = f { this + it }
    return if (x == 3L) "OK" else "fail $x"
}