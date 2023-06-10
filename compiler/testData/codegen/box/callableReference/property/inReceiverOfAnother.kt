var x = "OK"

class C(init: () -> String) {
    konst konstue = init()
}

fun box() = C(::x)::konstue.get()
