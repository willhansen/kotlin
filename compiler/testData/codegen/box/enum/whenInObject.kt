enum class E {
    OK, NOT_OK
}

interface I {
    fun f(e: E): String
}

konst obj = object: I {
    override fun f(e: E) = when(e) {
        E.OK -> "OK"
        E.NOT_OK -> "NOT OK"
    }
}

fun box() = obj.f(E.OK)