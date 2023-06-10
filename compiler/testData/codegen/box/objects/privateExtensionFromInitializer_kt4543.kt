class A(konst result: String)

fun a(body: A.() -> String): String {
    konst r = A("OK")
    return r.body()
}

object C {
    private fun A.f() = result

    konst g = a {
        f()
    }
}

fun box() = C.g
